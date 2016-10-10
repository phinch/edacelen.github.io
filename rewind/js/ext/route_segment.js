filename = "dc.json";
function loadPoints(){
    d3.json(filename, function(raw){
        var data = raw['locations'];
        data.sort(function(a,b) {return (a.timestampMs > b.timestampMs) ? 1 : ((b.timestampMs > a.timestampMs) ? -1 : 0);});


        //Data is now sorted in order of timestamp (ascending)

        //Maybe we can clean to eliminate bad data? (jumps in distance that don't make any sense)
        //Not doing this for now, just using the accuracy statistic given in data
        
        //Split the data to only be per week
        var data_by_week = []
        
        //Split up the data into JSONs every week, i.e. 604800000 milliseconds
        var currtime = -1
        var prevtime = -1
        var millisecs = 0
        var week_count = 0
        
        data_by_week.push({})
        week = data_by_week[week_count]
        week['locations'] = []
        
        for(var l in data){
            var line = data[l]
            week['locations'].push(line)

            currtime = parseInt(line['timestampMs'])
            if(prevtime == -1){
                prevtime = currtime
            }
            millisecs += currtime - prevtime
            prevtime = currtime

            if(millisecs >= 604800000/7){
                millisecs = 0
                week_count += 1
                data_by_week.push({})
                week = data_by_week[week_count]
                week['locations'] = []
            }
        }

        //We want to pull out an interesting route from the data
        //"Interesting" is subjective; I'll define some hopefully fairly standard characteristics here:
        // Route should be (somewhat) unique within the data
        // Route's starting point or destination should be an points of "interest" (POI) (again, subjective), or should contain POIs

        //As a basic way of identifying POIs, we can create a list of places where the traveller stays for some period of time
        //This might include a museum or a park, but also more mundane places like a hotel or a train station, for example
        //If the user starts from, ends at, or passes(?) along the way at one of these places, the route may become more interesting

        //Multiple methods for clustering; K-means may be infeasible because we can't know the number of clusters we want;
        // for now, I'm using DBSCAN and averaging the labels to find cluster centers
        // Disadvantage of DBSCAN is that I have to specify a set size for a cluster, which can be limiting
        // Other options are heirarchical clustering and K-Means.
        for(var w = 0; w < data_by_week.length; w++){
            data = data_by_week[w]['locations']
            latlngs = []
            latlngtime = []
            for(var l in data){
                var line = data[l]
                latlngs.push([parseFloat(line['latitudeE7'])/10000000, parseFloat(line['longitudeE7'])/10000000])
                latlngtime.push([parseFloat(line['latitudeE7'])/10000000, parseFloat(line['longitudeE7'])/10000000, parseInt(line['timestampMs'])])
            }
            var poiscanner = new DBSCAN();
            // This will return the assignment of each point to a cluster number, 
            // points which have  -1 as assigned cluster number are noise.
            var pois = poiscanner.run(latlngs, 0.0005, 10);

            //Associate points with their POIs
            scanner_clusters = {}
            for(var i = 0; i < pois.length; i++){
                scanner_clusters[i] = [0, 0, 0];
                cluster = pois[i];
                for(var j = 0; j < cluster.length; j++){
                    point = latlngs[cluster[j]];
                    scanner_clusters[i][0] += point[0];
                    scanner_clusters[i][1] += point[1];
                    scanner_clusters[i][2] += 1;

                    latlng = latlngtime[cluster[j]];
                    latlng.push(i);
                }

                //Average to obtain the center of the poi (for viz purposes)
                scanner_clusters[i][0] /= scanner_clusters[i][2];
                scanner_clusters[i][1] /= scanner_clusters[i][2];
            }

            for(var i = 0; i < poiscanner.noise.length; i++){
                latlng = latlngtime[poiscanner.noise[i]];
                latlng.push(-1);
            }
          
            //------------------------------
            //Another important feature is the uniqueness of a route
            //We can create an index measuring the uniqueness of each point, with a slight fuzzing of the data
            
            //Uniqueness here is binary, where anything that gets clustered is not unique 
            var uniquescanner = new DBSCAN();
            var uniques = uniquescanner.run(latlngs, 0.00005, 2);

            for(var i = 0; i < uniques.length; i++){
                scanner_clusters[i] = [0, 0, 0];
                cluster = uniques[i];
                for(var j = 0; j < cluster.length; j++){
                    point = latlngs[cluster[j]];
                    latlng = latlngtime[cluster[j]];
                    latlng.push(i);
                }
            }

            //-1 (noise) means unique; >= 0 means the point was clustered, so it's not unique
            for(var i = 0; i < uniquescanner.noise.length; i++){
                latlng = latlngtime[uniquescanner.noise[i]];
                latlng.push(-1);
            }

            //(Optional: for visualization)
            /*
            for(var i in scanner_clusters){
                cluster = scanner_clusters[i];
                addCircle(cluster[0], cluster[1], "#FF8726", 75);
            }

            for(var i = 0; i < latlngtime.length; i++){
                if(latlngtime[i][4] == -1){
                    addCircle(latlngtime[i][0], latlngtime[i][1], "green", 10);
                }
                if(latlngtime[i][4] != -1){
                    addCircle(latlngtime[i][0], latlngtime[i][1], "red", 10);
                }
            }
            */
            
            
            //------------------------------
            //We can partition the data into "routes"; for now, let's say a route ends when the user stays in a POI for 30 minutes (1800000 millisecs)
            //This allows the user to amble and stop at certain places within the larger picture of going somewhere, 
            // thereby flagging routes with interesting, but intermediate, stops
            //These routes are consecutive and non-overlapping (i.e. each lat/lng is in exactly one route)
            //However, a route is only valid if it goes outside of a POI (i.e. cannot exclusively be within a POI, like sleeping)
                
            routes = []
            curr_route = []
            poi_count = 0
            poi_time_count = 0
            prev_time = -1
            curr_time = -1
            curr_poi = -2
            changed_pois = false
            
            for(var i = 0; i < latlngtime.length; i++){
                point = latlngtime[i]
                latlng = [point[0], point[1]]
                curr_time = point[2]
                if(prev_time == -1){
                    prev_time = curr_time
                }
                label = point[3]
                //Not in POI: considered to be moving along a route
                if(label == -1){
                    poi_count = 0
                    poi_time_count = 0
                    curr_poi = -1
                    changed_pois = true
                }
                //In a POI: If we stay in this POI for too long, the route ends
                else{
                    poi_count += 1
                    poi_time_count += curr_time - prev_time
                    if(curr_poi != label && curr_poi != -2){
                        changed_pois = true
                        poi_count = 0
                        poi_time_count = 0
                    }
                    curr_poi = label
                    if(poi_time_count >= 300000){ //900000 = 15 minutes; 1800000 = 30 min
                        poi_count = 0
                        poi_time_count = 0
                        if(changed_pois && curr_route.length >= 10){
                            //Split the route into 10-minute pieces
                            splits = 0
                            start_time = curr_route[0][2]
                            end_time = start_time + 600000;
                            mini_route = []
                            for(var l in curr_route){
                                latlng = curr_route[l];
                                mini_route.push(latlng);
                                start_time += latlng[2]-start_time;
                                if(start_time >= end_time){
                                    splits++;
                                    routes.push(mini_route);
                                    end_time = start_time + 600000;
                                    mini_route = [];
                                }
                            }
                            if(splits > 0){
                                routes[routes.length-1].concat(mini_route)
                            }else{
                                routes.push(mini_route);
                            }
                        }
                        curr_route = [point]
                        changed_pois = false
                        continue
                    }
                }
                        
                //We only start tracking the route once we leave the POI we started in
                if(changed_pois){
                    curr_route.push(point)
                }
            }
            console.log(routes.length);
            
            //------------------------------
            //These two metrics, uniqueness and the presence of POIs, can be used to score routes and then select an interesting one
            //Experimentally, each unique lat/lng is worth 1 point, and each lat/lng spent in a POI is worth 1 point
            //Each lat/lng's score is modified by its accuracy(?)
            //Then the total score is divided by the total number of lat/lngs, to standardize scores
            
            route_scores = []
            for(var r in routes){
                var route = routes[r];
                var score = 0.0
                for(var l in route){
                    latlng = route[l];
                    if(latlng[4] == -1){ //Unique point
                        score += 1
                    }
                    if(latlng[3] != -1){ //POI
                        score += 1
                    }
                }
                //Normalize for number of points
                score /= route.length;
                route_scores.push([route, score])
            }
        
            top_route = [[], -1]
            for(var r in route_scores){
                route = route_scores[r];
                if(route[1] > top_route[1]){
                    top_route = route
                }
            }
                    
            top_route = top_route[0]
            route_lat_lngs = []

            for(var l in top_route){
                route_lat_lngs.push({lat: top_route[l][0], lng: top_route[l][1]});
                addCircle(top_route[l][0], top_route[l][1], "black", 10);
            }

            new google.maps.Polyline({
              path: route_lat_lngs,
              map: map,
              geodesic: true,
              strokeColor: 'black',
              strokeOpacity: 1.0,
              strokeWeight: 1
            });
        }
    });
}
