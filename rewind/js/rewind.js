$(function() {
    var Caman = require('caman');

    var API_KEY = 'AIzaSyC_GSlJw9b4ns8AHndV-EMp-kCA35ZAvSE';
   
    TIMEZONE_API = "https://maps.googleapis.com/maps/api/timezone/json?key=" + API_KEY + "&location={{LAT}},{{LON}}&timestamp={{MILLIS}}";


    var weather_filepath = '../data/clean.txt';

    // For northern hemisphere only!
    MONTH_SATURATION = {
        "12": -40,
        "01": -40,
        "02": -40,

        "03": 10,
        "04": 10,
        "05": 10,

        "06": 40,
        "07": 40,
        "08": 40,

        "09": -10,
        "10": -10,
        "11": -10,
    };

    var ambiance = null;
    var panoWidth  = 640;
    var panoHeight = 640;
    var directions_service;

    // TODO: load weather file uh figure out how to do this _properly_
    var weatherData = '201602270000+41733-0714330,201602270051+41722-0714330,201602270151+41722-0714330,201602270251+41722-0714330,201602270351+41722-0714330,201602270451+41722-0714330,201602270459+41722-0714331,201602270551+41722-0714331,201602270600+41733-0714330,201602270651+41722-0714331,201602270751+41722-0714331,201602270851+41722-0714331,201602270951+41722-0714331,201602271051+41722-0714331,201602271151+41722-0714331,201602271200+41733-0714330,201602271251+41722-0714331,201602271351+41722-0714331,201602271451+41722-0714331,201602271551+41722-0714331,201602271651+41722-0714331,201602271751+41722-0714331,201602271800+41733-0714330,201602271851+41722-0714331,201602271951+41722-0714331,201602272051+41722-0714331,201602272151+41722-0714331,201602272251+41722-0714331,201602272351+41722-0714331,201602280000+41733-0714330,201602280051+41722-0714331,201602280151+41722-0714331,201602280251+41722-0714331,201602280351+41722-0714331,201602280451+41722-0714331,201602280459+41722-0714331,201602280551+41722-0714331,201602280600+41733-0714330,201602280651+41722-0714331,201602280751+41722-0714331,201602280851+41722-0714331,201602280951+41722-0714331,201602281051+41722-0714331,201602281151+41722-0714331,201602281200+41733-0714330,201602281251+41722-0714331,201602281351+41722-0714331,201602281451+41722-0714331,201602281551+41722-0714331,201602281651+41722-0714331,201602281751+41722-0714331,201602281800+41733-0714330,201602281851+41722-0714331,201602281951+41722-0714331,201602282051+41722-0714331,201602282151+41722-0714331,201602282251+41722-0714331,201602282351+41722-0714331,201602290000+41733-0714330,201602290051+41722-0714331,201602290151+41722-0714331,201602290251+41722-0714331,201602290351+41722-0714331,201602290451+41722-0714331,201602290459+41722-0714331,201602290551+41722-0714331,201602290600+41733-0714330,201602290651+41722-0714331,201602290751+41722-0714331,201602290851+41722-0714331,201602290951+41722-0714331,201602291051+41722-0714331,201602291151+41722-0714331,201602291200+41733-0714330,201602291251+41722-0714331,201602291351+41722-0714331,201602291451+41722-0714331,201602291551+41722-0714331,201602291651+41722-0714331,201602291751+41722-0714331,201602291800+41733-0714330,201602291851+41722-0714331,201602291951+41722-0714331,201602292051+41722-0714331,201602292058+41722-0714330,201602292138+41722-0714330,201602292151+41722-0714331,201602292251+41722-0714331,201602292351+41722-0714331,201603010000+41733-0714331,201603010051+41722-0714331,201603010151+41722-0714331,201603010251+41722-0714331,201603010351+41722-0714331,201603010451+41722-0714331,201603010459+41722-0714331,201603010459+41722-0714330,201603010551+41722-0714331,201603010600+41733-0714330,201603010651+41722-0714331,201603010751+41722-0714331,201603010851+41722-0714331,201603010951+41722-0714331,201603011051+41722-0714331,201603011151+41722-0714331,201603011200+41733-0714330,201603011251+41722-0714331,201603011351+41722-0714331,201603011451+41722-0714331,201603011551+41722-0714331,201603011651+41722-0714331,201603011751+41722-0714331,201603011800+41733-0714330,201603011851+41722-0714331,201603011951+41722-0714331,201603012051+41722-0714331,201603012151+41722-0714331,201603012251+41722-0714331,201603012351+41722-0714331,201603020000+41733-0714330,201603020051+41722-0714331,201603020151+41722-0714331,201603020251+41722-0714331,201603020351+41722-0714331,201603020447+41722-0714330,201603020451+41722-0714331,201603020459+41722-0714331,201603020511+41722-0714331,201603020529+41722-0714331,201603020551+41722-0714331,201603020600+41733-0714331,201603020647+41722-0714331,201603020651+41722-0714331,201603020722+41722-0714331,201603020732+41722-0714330,201603020751+41722-0714331,201603020851+41722-0714331,201603020943+41722-0714331,201603020949+41722-0714330,201603020951+41722-0714331,201603021051+41722-0714331,201603021148+41722-0714331,201603021151+41722-0714331,201603021200+41733-0714331,201603021227+41722-0714331,201603021251+41722-0714331,201603021351+41722-0714331,201603021451+41722-0714331,201603021551+41722-0714331,201603021626+41722-0714330,201603021642+41722-0714330,201603021649+41722-0714330,201603021651+41722-0714331,201603021751+41722-0714331,201603021800+41733-0714331,201603021851+41722-0714331,201603021951+41722-0714331,201603022051+41722-0714331,201603022151+41722-0714331,201603022251+41722-0714331,201603022351+41722-0714331,201603030000+41733-0714330,201603030051+41722-0714331,201603030151+41722-0714331,201603030251+41722-0714331,201603030351+41722-0714331,201603030451+41722-0714331,201603030459+41722-0714331,201603030551+41722-0714331,201603030600+41733-0714330,201603030651+41722-0714331,201603030751+41722-0714331,201603030851+41722-0714331,201603030951+41722-0714331,201603031051+41722-0714331,201603031151+41722-0714331,201603031200+41733-0714331,201603031251+41722-0714331,201603031351+41722-0714331,201603031451+41722-0714331,201603031551+41722-0714331,201603031651+41722-0714331,201603031751+41722-0714331,201603031800+41733-0714330,201603031851+41722-0714331,201603031951+41722-0714331,201603032051+41722-0714331,201603032151+41722-0714331,201603032251+41722-0714331,201603032351+41722-0714331,201603040000+41733-0714330,201603040051+41722-0714331,201603040151+41722-0714331,201603040251+41722-0714331,201603040351+41722-0714331,201603040451+41722-0714331,201603040459+41722-0714331,201603040551+41722-0714331,201603040600+41733-0714330,201603040651+41722-0714331,201603040751+41722-0714331,201603040851+41722-0714331,201603040951+41722-0714331,201603041051+41722-0714331,201603041151+41722-0714331,201603041200+41733-0714330,201603041251+41722-0714331,201603041351+41722-0714331,201603041451+41722-0714331,201603041551+41722-0714331,201603041625+41722-0714331,201603041651+41722-0714331,201603041751+41722-0714331,201603041800+41733-0714331,201603041849+41722-0714331,201603041851+41722-0714331,201603041853+41722-0714331,201603041951+41722-0714331,201603041958+41722-0714331,201603042026+41722-0714331,201603042049+41722-0714330,201603042051+41722-0714331,201603042114+41722-0714331,201603042151+41722-0714331,201603042251+41722-0714331,201603042313+41722-0714330,201603042351+41722-0714331,201603050000+41733-0714331,201603050051+41722-0714331,201603050118+41722-0714330,201603050149+41722-0714330,201603050151+41722-0714331,201603050251+41722-0714331,201603050349+41722-0714330,201603050351+41722-0714331,201603050433+41722-0714331,201603050451+41722-0714331,201603050459+41722-0714331,201603050551+41722-0714331,201603050600+41733-0714331,201603050651+41722-0714331,201603050702+41722-0714331,201603050751+41722-0714331,201603050851+41722-0714331,201603050951+41722-0714331,201603051051+41722-0714331,201603051151+41722-0714331,201603051200+41733-0714331,201603051251+41722-0714331,201603051351+41722-0714331,201603051451+41722-0714331,201603051622+41722-0714330,201603051651+41722-0714331,201603051751+41722-0714331,201603051800+41733-0714330,201603051851+41722-0714331,201603051951+41722-0714331,201603052051+41722-0714331,201603052151+41722-0714331,201603052251+41722-0714331,201603060051+41722-0714331,201603060151+41722-0714331,201603060251+41722-0714331,201603060351+41722-0714331,201603060451+41722-0714331,201603060459+41722-0714331,201603060551+41722-0714330,201603060651+41722-0714330,';
    
    function createHyperlapse(locations, pano) {
        //$(pano).html("<img src='img/loading.gif' style='width:50px; margin:275px 275px'></img>");
        if (locations.length < 2) {
            alert("too few location points (count: " + locations.length + ") for the day");
            return;
        }
        
        var routes = sliceLocationsToRoutes(locations);
        var gResults = [];

        for (var i = 0; i < routes.length; i++) {
            getGoogleRoute(routes, gResults, i, function() {
                var routeSequence = StreetviewSequence($(pano), {
                    route: mergeGoogleResponses(gResults),
                    duration: 10000,
                    totalFrames: 20,
                    loop: true,
                    width: panoWidth,
                    height: panoHeight,
                    domain: 'http://maps.googleapis.com',
                    key: API_KEY,
                });

                routeSequence.done(function(player) {
                    $("#playButton").html("Play Rewind");
                    $("#playButton").css("background-color", "#009aff");
                    $("#playButton").css("color", "white");
                    $("#playButton").click(function() {
                        $(pano).find("img").hide();
                        $(pano).find("canvas").show();
                        $(pano).show();
                        $(pano.parentNode).find('.location').hide();
                        player.play();
                    });

                    // ambiance = new Audio("./audio/thunder.mp3");
                    // ambiance.play();

                    //when cursor is on rewind video
                    $(pano).on("mousemove", function(event) {
                        var mouseX = parseInt(event.pageX - parseInt($(pano).offset().left));
                        var mouseY = parseInt(event.pageY - parseInt($(pano).offset().top));
                        // console.log( "pageX: " + mouseX + ", pageY: " + mouseY );
                        var imagePos = mouseX / panoWidth;
                        //console.log("Frame progress:", imagePos);
                        player.setProgress(imagePos);
                    });
                });
            });
        }
        
    }

    function mergeGoogleResponses(googleResponses) {
        var firstResponse = googleResponses.shift();

        var newPath = firstResponse.routes[0].overview_path;
        var newLegs = firstResponse.routes[0].legs;

        _.each(googleResponses, function(resp) {
            var path = resp.routes[0].overview_path;
            var legs = resp.routes[0].legs;

            newPath = newPath.concat(path);
            newLegs = newLegs.concat(legs);
        });

        firstResponse.routes[0].overview_path = newPath;
        firstResponse.routes[0].legs = newLegs;

        return firstResponse;
    }

    function getGoogleRoute(routes, results, i, onFinished) {
        var locations = routes[i];

        var first = locations.shift();
        var last = locations.pop();
        
        directions_service = new google.maps.DirectionsService();

        var routeRequest = {
            origin: googleLatLng(first),
            destination: googleLatLng(last),
            waypoints: _(locations).map(function(location) {
                return {
                    location: googleLatLng(location)
                };
            }),
            travelMode: google.maps.DirectionsTravelMode.DRIVING
        };

        getRouteFromDirectionsService(routeRequest, 10, function(err, response) {
            if (err) {
                throw Error("Direction Service request failed for: " + JSON.stringify(err));
            }

            results[i] = response;

            if (_.compact(results).length == routes.length) {
                onFinished();
            }
        });
    }

    function getRouteFromDirectionsService(request, tries, callback) {
        setTimeout(function() { 
            directions_service.route(request, function(response, status) {
                if (status == google.maps.DirectionsStatus.OK) {
                    callback(null, response);
                } else if (tries > 0) {
                    console.log('retrying with ' + tries + ' left');
                    getRouteFromDirectionsService(request, tries - 1, callback); 
                } else {
                    console.log(status);
                    callback(status, null);
                }
            });            
        }, Math.random() * 10000);
    }

    function sliceLocationsToRoutes(locations) {
        var routeMax = 10;
        var locs = [];
        var routes = [];

        while (locations.length > 0) {
            var loc = locations.shift();
            locs.push(loc);

            if (locs.length == routeMax || locations.length == 0) {
                routes.push(locs);
                locs = [loc];
            }
        }

        return routes;

    }

    function googleLatLng(location) {
        return new google.maps.LatLng(location.latitude, location.longitude);
    }

    // inspired from StackOverflow: http://stackoverflow.com/a/13763063/1246009
    function getImageLightnessSampled(image) {
        // constants
        var sampleCount = 10000; // number of samples
        
        var canvas = document.createElement("canvas");
        canvas.width = image.width;
        canvas.height = image.height;

        var ctx = canvas.getContext("2d");
        ctx.drawImage(image, 0, 0);

        var imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        var data = imageData.data;
        var colorSum = 0, saturSum = 0;

        for (var i = 0; i < sampleCount; i++) {
            var randX = Math.floor(Math.random() * canvas.width);
            var randY = Math.floor(Math.random() * canvas.height);

            var x = (randY * canvas.width) + randX;

            var r = data[x] / 256;
            var g = data[x + 1] / 256;
            var b = data[x + 2] / 256;

            colorSum += (r + g + b) / 3;

            var cmin = Math.min(r, g, b);
            var cmax = Math.max(r, g, b);
            var cdelta = cmax - cmin;

            saturSum += (cmax != 0 ? cdelta / cmax : 0);
        }

        var brightness = colorSum / sampleCount;
        var saturation = saturSum / sampleCount;
        return [brightness, saturation];
    }

    function getImageLightnessFull(image) {
        var canvas = document.createElement("canvas");
        canvas.width = image.width;
        canvas.height = image.height;

        var ctx = canvas.getContext("2d");
        ctx.drawImage(image, 0, 0);

        var imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        var data = imageData.data;
        var colorSum = 0, saturSum = 0;

        for (var x = 0, len = data.length; x < len; x += 4) {
            var r = data[x] / 256;
            var g = data[x + 1] / 256;
            var b = data[x + 2] / 256;

            colorSum += (r + g + b) / 3;

            var cmin = Math.min(r, g, b);
            var cmax = Math.max(r, g, b);
            var cdelta = cmax - cmin;

            saturSum += (cmax != 0 ? cdelta / cmax : 0);
        }

        var brightness = colorSum / (image.width * image.height);
        var saturation = saturSum / (image.width * image.height);
        return [brightness, saturation];

        // return [0.5, 0.5];
    }

    var dragArea = document.getElementById('file-drop-area');
    dragArea.ondragover = function () { this.className = 'dragging'; return false; };
    dragArea.ondragend = dragArea.ondragleave = function () { this.className = ''; return false; };
    dragArea.ondrop = function (e) {
        this.className = '';
        e.preventDefault();
        handleUploadedFile(e.dataTransfer.files[0]);
    }

    var fileInput = document.getElementById('fileInput');
    fileInput.addEventListener('change', function(e) {
        handleUploadedFile(fileInput.files[0]);
    });

    function handleUploadedFile(file) {
        $("#upload-wrapper").html("<img src='img/loading.gif' style='width:50px; margin: 20px 275px'></img>");

        var reader = new FileReader();

        reader.readAsText(file);
        reader.addEventListener('load', function(e) {
            //processLocationExport(reader.result);
            pullRoutesfromLocs(reader.result);
        });
    }

    function isWinter(month, lat) {
        var absLat = Math.abs(parseInt(lat));

        return (_.contains(["12", "01", "02"], month) || absLat > 60) && absLat > 15;
    }

    function isSummer(month, lat) {
        var absLat = Math.abs(parseInt(lat));

        return (_.contains(["06", "07", "08"], month) || absLat < 15) && absLat < 60;
    }

    function isSpring(month, lat) {
        var absLat = Math.abs(parseInt(lat));

        return _.contains(["03", "04", "05"], month);
    }

    function isFall(month, lat) {
        var absLat = Math.abs(parseInt(lat));

        return _.contains(["09", "10", "11"], month);
    }

    var locationsByDate = null;

    var timeZoneUrlTpl = "https://maps.googleapis.com/maps/api/timezone/json?key=" + API_KEY + "location={{LAT}},{{LON}}&timestamp={{MILLIS}}";

    var timezoneJSONs = {};
    function getTimezoneJSON(url, callback) {
        if (timezoneJSONs[url]) { 
            callback(timezoneJSONs[url]);
        } else {
            $.getJSON(url, function(data) {
                timezoneJSONs[url] = data;
                callback(data);
            });
        }
    }

    function manipulateImage(image, millis, lat, lon, callback) {
        var datetime = moment(parseInt(millis));
        var hour = datetime.utc().hour();
        var timeZoneUrl = timeZoneUrlTpl
                .replace("{{LAT}}", lat)
                .replace("{{LON}}", lon)
                .replace("{{MILLIS}}", millis.replace(/\d\d\d$/,""));

        // var exposure = hour > 12 ? -60 : 40;

        // console.log("Manipulating:", image);

        var month = getMonth(datetime.format("YYYY-MM-DD"));
        // var saturation = MONTH_SATURATION[month];

        // TODO : account for some hours that are skipped in data
        var re = new RegExp(datetime.format('YYYYMMDDHH')+'[^,]{16}');
        var myWeather = weatherData.match(re);
        var precip = 0;
        if (myWeather) {
            myWeather = myWeather[0];
            precip = myWeather.substring(25);
        }
        console.log(myWeather+" precipitation? "+precip);
        // var sprinkle = [11, 12, 13, 14, 15];
        // var drizzle = [17, 18, 19, 20, 21];
        // var rain = [23, 24, 25, 26, 27];
        // var pour = [28, 29, 30, 31, 32];
        // if (precip == 4) {
        //     var noise = pour[Math.floor(Math.random()*pour.length)];
        // }
        // else if (precip == 3) {
        //     var noise = rain[Math.floor(Math.random()*rain.length)];
        // }
        // else if (precip == 2) {
        //     var noise = drizzle[Math.floor(Math.random()*drizzle.length)];
        // }
        // else if (precip == 1) {
        //     var noise = sprinkle[Math.floor(Math.random()*sprinkle.length)];
        // }
        var contrast = 0;
        if (Math.floor(lat)==41 && Math.floor(lon)==-71 && precip > 0) {
            contrast = -15;
        } else {
            contrast = 10;
        }
        
        getTimezoneJSON(timeZoneUrl, function(data) {
            var hourOffset = data.rawOffset / 60 / 60;
            var localHour = ( hour + hourOffset + 24 ) % 24;

            // console.log("Received location data for:", image);

            var res = getImageLightnessSampled(image);
            var avgBrightness = res[0];
            var avgSaturation = res[1];

            var saturation = 0;
            var exposure = 0;

            if (localHour > 16 && localHour <= 19 && avgBrightness > 0.5) {
                exposure = -20;
            } else if ((localHour > 19 || localHour <= 6) && avgBrightness > 0.3) {
                exposure = -40;
            } else if (localHour > 6 && localHour <= 12 && avgBrightness <= 0.5) {
                exposure = 40;
            } else if (localHour > 12 && localHour <= 16 && avgBrightness <= 0.3) {
                exposure = 20;
            }

            if (isWinter(month, lat) && avgSaturation > 0.10) {
                saturation = -40;
            } else if (isSpring(month, lat) && avgSaturation > 0.30) {
                saturation = -20;
            } else if (isSpring(month, lat) && avgSaturation < 0.20) {
                saturation = 20;
            } else if (isSummer(month, lat) && avgSaturation < 0.30) {
                saturation = 40;
            } else if (isFall(month, lat) && avgSaturation > 0.20) {
                saturation = -20;
            } else if (isFall(month, lat) && avgSaturation < 0.10) {
                saturation = 20;
            }

            if (parseInt(lat) < 0) {
                saturation *= -1;
            }

            // if (callback) {
            //     saturation = -80;
            //     exposure = 40;
            // }

            var parent = image.parentNode; 
            if (!parent) {
                parent = document.createElement("div");
                parent.appendChild(image);
            }

            // log data on image attributes
            $(image).attr("data-saturation", saturation);
            $(image).attr("data-exposure", exposure);
            $(image).attr("data-contrast", contrast);
            $(image).attr("data-hour", hour);
            $(image).attr("data-localHour", localHour);
            
            Caman.fromImage(image).then(function(caman) {
                caman.attach(image);

                var newImage = parent.childNodes[0];

                // copy image attributes to canvas attributes
                $(newImage).attr("class", $(image).attr("class"));
                $(newImage).attr("data-date", $(image).attr("data-date"));
                $(newImage).attr("data-millis", $(image).attr("data-millis"));
                $(newImage).attr("data-lat", $(image).attr("data-lat"));
                $(newImage).attr("data-lon", $(image).attr("data-lon"));
                $(newImage).attr("data-saturation", $(image).attr("data-saturation"));
                $(newImage).attr("data-exposure", $(image).attr("data-exposure"));
                //$(newImage).attr("data-noise", $(image).attr("data-noise"));
                $(newImage).attr("data-hour", $(image).attr("data-hour"));
                $(newImage).attr("data-localHour", $(image).attr("data-localHour"));

                return caman.pipeline(function () {
                    this.saturation(saturation);
                    this.exposure(exposure);
                    this.contrast(contrast);

                }).then(function () {
                    if (callback)  {
                        callback(parent.childNodes[0]);
                    }
                });
            });
        });
    }

    function getRandomLocations(locationsByDate, count, callback) {
        var randLocations = [];
        var i = 0;
        var dates = _.keys(locationsByDate);
        var tries = count * 2;
        var tried = 0;
        var completed = false;
        
        while (i < tries) {
            var date = dates[Math.floor(Math.random() * dates.length)];
            var dateLocations = getLocationsOnDate(date);

            if (dateLocations.length > 2) {
                i++;

                (function() {
                    var locDate = date;
                    var location = dateLocations[Math.floor(Math.random() * dateLocations.length)];
                    var streetviewUrl = generateStreetViewUrl(location);
                    $.get(streetviewUrl, function(imageData) {
                        tried++;

                        if (imageData.length < 10000) {
                            console.log("Gray image!", streetviewUrl);
                            return; // gray image
                        }
                        if (completed) return; // we've already called the callback

                        randLocations.push({
                            date: locDate,
                            location: location
                        });

                        if (randLocations.length == count || tried == tries) {
                            // we've collected enough or tried enough
                            completed = true;
                            callback(randLocations);
                        }
                    });
                })();
            }
        }
    }

    function getMostFrequentLocations(ascLocsWithFreqs, count, callback) {
        var mostFreqLocations = [];
        var tries = count * 2;
        var tried = 0;
        var completed = false;

        var locsToTry = _.last(ascLocsWithFreqs, tries).map(function(locWithFreq) { return locWithFreq.locDate; });
        
        locsToTry.forEach(function(location) {
            var streetviewUrl = generateStreetViewUrl(location);
            $.get(streetviewUrl, function(imageData) {
                tried++;

                if (imageData.length < 10000) {
                    console.log("Gray image!", streetviewUrl);
                    return; // gray image
                }
                if (completed) return; // we've already called the callback

                mostFreqLocations.push({
                    date: location.date,
                    location: location
                });

                if (mostFreqLocations.length == count || tried == tries) {
                    // we've collected enough or tried enough
                    completed = true;
                    callback(mostFreqLocations);
                }
            });
        });
    }

    function getLeastFrequentLocations(ascLocsWithFreqs, count, callback) {
        var leastFreqLocations = [];
        var tries = count * 2;
        var tried = 0;
        var completed = false;

        var locsToTry = _.first(ascLocsWithFreqs, tries).map(function(locWithFreq) { return locWithFreq.locDate; });
        
        locsToTry.forEach(function(location) {
            var streetviewUrl = generateStreetViewUrl(location);
            $.get(streetviewUrl, function(imageData) {
                tried++;

                if (imageData.length < 10000) {
                    console.log("Gray image!", streetviewUrl);
                    return; // gray image
                }
                if (completed) return; // we've already called the callback

                leastFreqLocations.push({
                    date: location.date,
                    location: location
                });

                if (leastFreqLocations.length == count || tried == tries) {
                    // we've collected enough or tried enough
                    completed = true;
                    callback(leastFreqLocations);
                }
            });
        });
    }

    function getMonth(dateString) {
        return dateString.split("/")[0];
    }

    function sortLocationsByFreq(locations) {
        var freqs = {};

        locations.forEach(function(location) {
            // take first 3 digits of lat/lon
            var lat = ("" + location.latitude).match(/^(.+\.\d{1,3})/)[1];
            var lon = ("" + location.longitude).match(/^(.+\.\d{1,3})/)[1];

            var key = "" + lat + "," + lon;

            if (!freqs[key]) {
                freqs[key] = [];
            }

            freqs[key].push(location);
        });

        var locsWithFreqs = [];

        _.each(freqs, function(locs, key) { 
            locsWithFreqs.push({
                freq: locs.length, // how many times this loc visited
                locDate: locs[0]   // first time is taken for sample
            });
        });

        // ascending sorted version
        return locsWithFreqs.sort(function(a, b) { return a.freq - b.freq; });
    }

    // Haversine formula for coordinate distance calculation
    // From: http://stackoverflow.com/a/27943
    function getDistanceFromLatLonInKm(lat1,lon1,lat2,lon2) {
        function deg2rad(deg) {
            return deg * (Math.PI/180)
        }

        var R = 6371; // Radius of the earth in km
        var dLat = deg2rad(lat2-lat1);  // deg2rad below
        var dLon = deg2rad(lon2-lon1); 
        var a = 
            Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * 
            Math.sin(dLon/2) * Math.sin(dLon/2); 
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
        var distKm = R * c; // Distance in km

        return distKm;
    }

    function distance(locA, locB) {
        return getDistanceFromLatLonInKm(
            locA.latitude, locA.longitude, 
            locB.latitude, locB.longitude
        );
    }

    function markLocationsSurveyMeta(locations, home, type) {
        locations.forEach(function (location) {
            location.pickType = type;
            location.homeDist = distance(home, location.location);
        });
    }

    function processLocationExport(fileContents) {

        locationsByDate = parseLocationJson(fileContents);

        var ascLocsWithFreqs = sortLocationsByFreq(
            _.flatten(
                _.map(locationsByDate, function(locs, date) {
                    return locs.length > 5 ? locs : [];
                })
            )
        );

        var imageCount = 6;
        var mostFreqImageCount = 2;
        var leastFreqImageCount = 3;
        var randomImageCount = imageCount - mostFreqImageCount - leastFreqImageCount;

        getMostFrequentLocations(ascLocsWithFreqs, mostFreqImageCount, function(mostFreqLocations) {
            getLeastFrequentLocations(ascLocsWithFreqs, leastFreqImageCount, function(leastFreqLocations) {
                getRandomLocations(locationsByDate, randomImageCount, function(randomLocations) {
                    var homeLocation = _.last(ascLocsWithFreqs).locDate;

                    markLocationsSurveyMeta(mostFreqLocations, homeLocation, 'M');
                    markLocationsSurveyMeta(leastFreqLocations, homeLocation, 'L');
                    markLocationsSurveyMeta(randomLocations, homeLocation, 'R');

                    var locations = _.shuffle(_.flatten([mostFreqLocations, randomLocations, leastFreqLocations]));

                    processLocations(locations);
                });
            });
        });

    }

    function pullRoutesfromLocs(fileContents){
        var days = parseLocationJson(fileContents);
        for(var d in days){
            days[d].sort(function(a,b) {return (a.millis > b.millis) ? 1 : ((b.millis > a.millis) ? -1 : 0);});
        }

        //Data is now split into days sorted in order of timestamp (ascending)

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

        //We'll find the best route from each day and return the starting latitude and longitude 
        var route_starts = []
        for(var d in days){
            data = days[d]
            latlngs = []
            latlngtime = []
            for(var l in data){
                var line = data[l]
                latlngs.push([parseFloat(line['latitude']), parseFloat(line['longitude'])])
                latlngtime.push([parseFloat(line['latitude']), parseFloat(line['longitude']), parseInt(line['millis'])])
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
                    if(poi_time_count >= 300){ //300 = 5 minutes; 900 = 15 minutes
                        poi_count = 0
                        poi_time_count = 0
                        if(changed_pois && curr_route.length >= 10){
                            //Split the route into 10-minute pieces
                            splits = 0
                            start_time = curr_route[0][2]
                            end_time = start_time + 600;
                            mini_route = []
                            for(var l in curr_route){
                                latlng = curr_route[l];
                                if(start_time >= end_time && mini_route.length >= 5){
                                    splits++;
                                    console.log(mini_route);
                                    routes.push(mini_route);
                                    start_time = latlng[2];
                                    end_time = start_time + 600;
                                    console.log(start_time, end_time);
                                    mini_route = [];
                                }else if(start_time >= end_time){
                                    start_time = latlng[2];
                                    end_time = start_time + 600;
                                    mini_route = [];
                                }
                                mini_route.push(latlng);
                                start_time += latlng[2]-start_time;
                            }
                            if(splits > 0){
                                routes[routes.length-1].concat(mini_route)
                            }else if(mini_route.length >= 5){
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
            console.log(routes.length + " routes found on " + d);
            
            if(routes.length == 0){
                continue;
            }
            
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
                    
            top_route = top_route[0];
            route_starts.push(top_route[0]);
        }
        for(var r in route_starts){
            route_starts[r]['latitude'] = route_starts[r][0];
            route_starts[r]['longitude'] = route_starts[r][1];
            route_starts[r]['millis'] = route_starts[r][2];
            route_starts[r]['date'] = moment.unix(route_starts[r]['millis']).format('YYYY-MM-DD');
        }
        processLocations(route_starts, days);
    }

    function processLocations(locations, days) {
        console.log(locations);
        $("#upload-wrapper").hide();

        var imageIndex = 0;

        //var urls = generateStreetViewUrls(locations, false);

        // making this true filters the flatLocations array, filtering similar locations. 
        // Cannot match dates with locations in this case. urls does not match flatLocations.
        var urls = generateStreetViewUrls(locations, false);

        var questionsHtml = "";

        var questionHtmlTpl = "" +
            "<div class='location-questions' id='q{{INDEX}}'>" +
            "<div class='image-pano' style='position:relative'>" +
                "<img class='location' crossorigin='anonymous' src='{{SRC}}' data-date='{{DATE}}' data-millis='{{MILLIS}}' data-lat='{{LAT}}' data-lon='{{LON}}' style='width:"+ panoWidth +"px; height:"+ panoHeight +"px;'></img>" +
                "<img class='play-icon' src='img/play.png' style='position:absolute; top:0px; left:0px; width:100px; margin:270px 270px;'>"+
                // "<img class='loading-gif' src='img/loading.gif' style='position:absolute; top:0px; left:0px; width:100px; margin:270px 270px;'></img>"+
                "<div class='hyperlapse' style='display:none'></div>" +
            "</div>" +
            //"<ol>" +
            // "<li class='question'>" +
            // "<div class='qtext'> Do you remember this place?</div>" +
            // "<div class='qanswers'>" +
            //     "<input type='radio' name='q{{INDEX}}ans1' value='true'></input><span class='ans-label'> Yes</span>" +
            //     "<input type='radio' name='q{{INDEX}}ans1' value='false'></input><span class='ans-label'> No</span>" +
            // "</div>" +
            // "</li>" +
            // // "<li class='question'>" +
            // // 	"<div class='qtext'> Is this an important place to you?</div>" +
            // // 	"<div class='qanswers'>" +
            // // 		"<input type='radio' name='q{{INDEX}}ans2' value='true'></input><span class='ans-label'> Yes</span>" +
            // // 		"<input type='radio' name='q{{INDEX}}ans2' value='false'></input><span class='ans-label'> No</span>" +
            // // 	"</div>" +
            // // "</li>" +
            // "<li class='question'>" +
            // "<div class='qtext'> Do you want to keep this picture?</div>" +
            // "<div class='qanswers'>" +
            // "<input type='radio' name='q{{INDEX}}ans3' value='true'></input><span class='ans-label'> Yes</span>" +
            // "<input type='radio' name='q{{INDEX}}ans3' value='false'></input><span class='ans-label'> No</span>" +
            // "</div>" +
            // "</li>" +
            // "<li class='question' style='display:none'>" +
            // "<div class='qtext'> Were you traveling alone?</div>" +
            // "<div class='qanswers'>" +
            // "<input type='radio' name='q{{INDEX}}ans4' value='true'></input><span class='ans-label'> Yes</span>" +
            // "<input type='radio' name='q{{INDEX}}ans4' value='false'></input><span class='ans-label'> No</span>" +
            // "</div>" +
            // "</li>" +
            //"</ol>" +
            "<pre class='location-meta'>{{LOCMETA}}</pre>" +
            "<div style='clear: both;''></div>" +
            "</div>";


        urls.forEach(function(url, i) {
            var questionHtml = questionHtmlTpl
                .replace("{{SRC}}", url)
                .replace(/{{INDEX}}/g, i)
                .replace("{{DATE}}", locations[i].date)
                .replace("{{LAT}}", locations[i].latitude)
                .replace("{{LON}}", locations[i].longitude)
                .replace("{{MILLIS}}", locations[i].millis)
                //.replace("{{LOCMETA}}", "" + locations[i].pickType + (locations[i].homeDist * 1000).toFixed(0));

            questionsHtml += questionHtml;
        });


        var $resDiv = $("#question-list");
        $resDiv.html(questionsHtml);

        $(".image-pano > img.location").each(function() {
            var millis = $(this).attr("data-millis");
            var lat = $(this).attr("data-lat");
            var lon = $(this).attr("data-lon");

            this.onload = function() {
                manipulateImage(this, millis, lat, lon);
            };
        });

        $(".image-pano").click(function() {
            if ($(this).attr('class') != 'image-pano loading-hyperlapse') {
                $("#playButton").html("Loading Rewind...");
                $("#playButton").css("background-color", "white");
                $("#playButton").css("color", "#009aff");
                $("#playButton").visible();

                $(this).addClass("loading-hyperlapse");
                var $img = $(this).find(".location");
                console.log(days[$img.attr("data-date")]);
                var locations = getRoutefromStart($img.attr("data-millis"), days[$img.attr("data-date")]);

                var millis = $img.attr("data-millis");
                var lat = $img.attr("data-lat");
                var lon = $img.attr("data-lon");

                // $img.hide();
                // $img.nextAll(".play-icon").hide();
                // $img.nextAll(".hyperlapse").show();

                window.modifyHyperlapseImages = function(image, callback) {
                    manipulateImage(image, millis, lat, lon, callback);
                    // callback(image);
                };

                console.log('Creating Rewind from '+$img.attr("data-date")+' at ('
                +$img.attr("data-lat")+', '+$img.attr("data-lon")+')');

                createHyperlapse(locations, $img.nextAll(".hyperlapse")[0]);
            }

        });

        $(".location-questions > ol > li:first-child input").change(function() {
            if (this.value == "true") {
                $(this.parentNode.parentNode.parentNode).find("li").show();
            }
        });

        $(".location-questions > ol > li input").change(function() {
            $(this.parentNode).find("span.ans-label").css({
                color: ""
            });

            if (this.checked == true) {
                $(this).next().css({
                    fontWeight: "bold",
                    color: "#7acaff"
                });
            }
        });

        $(".location-questions").each(function() {
            $(this).hide();
        });

        $("#q0").show();
        $("#nextButton").visible();

        $("#submitButton").click(function() {
            var questions = $(".location-questions");
            var answers = [];
            // if (ambiance) ambiance.pause();

            questions.each(function(i, locQuestion) {
                var url = $(locQuestion).find("img").attr("src");
                var questions = $(locQuestion).find(".question");

                var answer = {
                    "url": url,
                    "q1": $(questions[0]).find("input:checked").attr("value") || null,
                    "q2": $(questions[1]).find("input:checked").attr("value") || null,
                    "q3": $(questions[2]).find("input:checked").attr("value") || null,
                };

                answers.push(answer);
            });

            console.log(answers);

            yesCounter = 0;
            answers.forEach(function(answer) {
                if (answer["q1"] == "true")
                    yesCounter++;
            });
            alert("You remember " + yesCounter + " our of " + answers.length + " places");
        });

        $("#nextButton").click(function() {
            $("div.hyperlapse").hide().find("*").remove();
            $("img.location, canvas.location").show().parent().removeClass("loading-hyperlapse");
            // if (ambiance) ambiance.pause();


            // $("#submitButton").visible();
            $("#backButton").visible();
            if (imageIndex < urls.length - 1) {
                $("#q" + imageIndex).hide();
                imageIndex++;
            }
            if (imageIndex == urls.length - 1) {
                $("#nextButton").invisible();
            }
            $("#q" + imageIndex).show();

            var $img = $(".image-pano").find(".location");

            // console.log("Showing: #q" + imageIndex + " imageIndex: " + imageIndex + " imageCount: " + urls.length);

            //if ($("#playButton")) $("#playButton").remove();
            $("#playButton").off();
            $("#playButton").invisible();
        });

        $("#backButton").click(function() {
            $("div.hyperlapse").hide().find("*").remove();
            $("img.location, canvas.location").show().parent().removeClass("loading-hyperlapse");
            // if (ambiance) ambiance.pause();

            $("#nextButton").visible();
            // $("#submitButton").visible();
            // if (ambiance) ambiance.pause();
            if (imageIndex > 0) {
                imageIndex--;
            }
            if (imageIndex == 0) {
                $("#backButton").invisible();
            }
            $("#q" + (imageIndex + 1)).hide();
            $("#q" + imageIndex).show();
            // console.log("Showing: #q" + imageIndex + " imageIndex: " + imageIndex + " imageCount: " + urls.length);

            //if ($("#playButton")) $("#playButton").remove();
            $("#playButton").off();
            $("#playButton").invisible();
        });
    }

    function getRoutefromStart(millis, day){
        //Find the start of the route within the day
        var p = 0;
        for(; p < day.length; p++){
            if(day[p]['millis'] == millis){
                break;
            }   
        }
        start = day[p];
        console.log(start);
        //Pull out 10 minutes
        route_points = [start]
        end_time = parseInt(start['millis']) + 600; //number of seconds later to end the route (e.g. 600 = 10 minutes)
        p++;
        for(; p < day.length; p++){
            point = day[p];
            if(parseInt(point['millis']) >= end_time){
                break;
            }
            route_points.push(point);
        }
        return route_points;
    }

    function getLocationsOnDate(date) {
        var locationHtml = "";

        var locations = locationsByDate[date];
        locations.sort(function(a, b) {
            return a.millis - b.millis;
        });

        var uniqLocations = []
        var uniq = {}

        locations.forEach(function(location, i) {
            var date = location.millis;
            // always get up to the first 7 digits after decimal
            var lat = ("" + location.latitude).match(/^(.+\.\d{1,7})/)[1];
            var lon = ("" + location.longitude).match(/^(.+\.\d{1,7})/)[1];


            // location unique to 3 digits after decimal point
            var latSig = ("" + lat).match(/^(.+\.\d{1,3})/)[1];
            var lonSig = ("" + lon).match(/^(.+\.\d{1,3})/)[1];
            var key = latSig + "_" + lonSig;

            if (uniq[key]) {
                return;
            } else {
                uniq[key] = true;
            }

            //locationHtml += "Date:" + moment(date).format("YYYY-MM-DD H:mm:ss") + " Lat:" + lat + " Lon:" + lon + "\n";
            uniqLocations.push(location);
        });

        //$("#urlList").html("<pre>"+locationHtml+"</pre>")
        return uniqLocations;
    }

    function generateStreetViewUrls(locations, uniq) {
        var urls = [];
        var uniq = {}

        locations.forEach(function(location, i) {
            urls.push(generateStreetViewUrl(location));
        });

        return urls;
    }

    function generateStreetViewUrl(location) {
        // always get up to the first 7 digits after decimal
        var lat = ("" + location.latitude).match(/^(.+\.\d{1,7})/)[1];
        var lon = ("" + location.longitude).match(/^(.+\.\d{1,7})/)[1];

        var streetViewUrl = "https://maps.googleapis.com/maps/api/streetview?key=" + API_KEY + "&size="+ panoWidth +"x"+ panoHeight +"&location=" + lat + "," + lon + "&fov=90&heading=270&pitch=10";

        return streetViewUrl;
    }

    function parseLocationJson(locJson) {
        var obj = JSON.parse(locJson);
        var results = {};

        obj.locations.forEach(function(location, i) {
            if (location.accuracy > 200) return;

            var lat = location.latitudeE7 * 0.0000001;
            var lon = location.longitudeE7 * 0.0000001;
            //console.log("Latitude: " + lat + "    Longitude: " + lon);

            //time checks
            //convert to seconds
            var timeMs = parseInt(location.timestampMs);
            if(timeMs / 10000000000 > 1){
                location.timestampMs = Math.round(parseInt(location.timestampMs)/1000);
                timeMs = location.timestampMs;
            }
            var date = moment.unix(timeMs).format("YYYY-MM-DD");
            if (!results[date]) {
                results[date] = [];
            }
            results[date].push({
                latitude: lat,
                longitude: lon,
                millis: timeMs,
                date: date
            });
        });

        return results;
    };

});
