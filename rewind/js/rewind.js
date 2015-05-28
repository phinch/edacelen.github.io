$(function() {
	TIMEZONE_API = "https://maps.googleapis.com/maps/api/timezone/json?location={{LAT}},{{LON}}&timestamp={{MILLIS}}";

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

    function createHyperlapse(locations, pano) {
        var panoWidth = 600;
        var panoHeight = 600;

        $(pano).html("<img src='img/loading.gif' style='width:50px; margin:275px 275px'></img>");

        if (locations.length < 2) {
            alert("too few location points (count: " + locations.length + ") for the day");
            return;
        }

        var hyperlapse = new Hyperlapse(pano, {
            //lookat: new google.maps.LatLng(41.842085, -71.394373),//(37.81409525128964,-122.4775045005249),
            zoom: 2,
            use_lookat: false,
            elevation: 50,
            width: panoWidth,
            height: panoHeight,
            millis: 1000 / 24,
            distance_between_points: 1,
            max_points: 300
        });

        hyperlapse.onError = function(e) {
            console.log(e);
        };

        hyperlapse.onRouteComplete = function(e) {
            hyperlapse.load();
        };

        hyperlapse.onLoadComplete = function(e) {
            $(pano).find("img").hide();
            $(pano).find("canvas").show();

            hyperlapse.play(); //adding slider instead.
            // $("#slider").attr("min", 0);
            // $("#slider").attr("max", hyperlapse.getPositionCount()-1);
            // $("#slider").val(0);
            // $("#slider").show();
            // //hyperlapse.setPosition(0);
            // $("#slider").on("input", function(){
            // 	var sliderPos = parseInt($("#slider").val());
            // 	//console.log(sliderPos);
            // 	hyperlapse.setPosition(sliderPos);
            // });
            //when cursor is on rew\ind video
            $(pano).on("mousemove", function(event) {
                var mouseX = parseInt(event.pageX - parseInt($(pano).offset().left));
                var mouseY = parseInt(event.pageY - parseInt($(pano).offset().top));
                // console.log( "pageX: " + mouseX + ", pageY: " + mouseY );
                var imagePos = parseInt(mouseX * (hyperlapse.getPositionCount() - 1) / panoWidth);
                console.log(imagePos);
                hyperlapse.setPosition(imagePos);
            });
        };

        var routes = sliceLocationsToRoutes(locations);
        var gResults = [];

        for (var i = 0; i < routes.length; i++) {
            getGoogleRoute(routes, gResults, i, function() {
                hyperlapse.generate({
                    route: mergeGoogleResponses(gResults)
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

        var first = locations.shift()
        var last = locations.pop()

        // Google Maps API stuff here...
        var directions_service = new google.maps.DirectionsService();

        var route = {
            request: {
                origin: googleLatLng(first),
                destination: googleLatLng(last),
                waypoints: _(locations).map(function(location) {
                    return {
                        location: googleLatLng(location)
                    };
                }),
                travelMode: google.maps.DirectionsTravelMode.DRIVING
            }
        };

        directions_service.route(route.request, function(response, status) {
            if (status == google.maps.DirectionsStatus.OK) {
                //hyperlapse.generate( {route:response} );
                results[i] = response;

                if (_.compact(results).length == routes.length) {
                    onFinished();
                }
            } else {
                console.log(status);
            }
        });
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

    // $( "#datePicker" ).datepicker({
    // 	onSelect: function(dateText, inst) { 
    // 		var dateAsString = dateText; //the first parameter of this function
    // 		var dateAsObject = $(this).datepicker( 'getDate' ); //the getDate method
    // 		$( "#date" ).text(dateAsString);
    // 		var locations = getLocationsOnDate(dateAsString);
    // 		createHyperlapse(locations);
    // 	},

    // 	beforeShowDay: function(date) {
    // 		console.log(date);
    // 		var formattedDate = moment(date).format("MM/DD/YYYY");
    // 		var enabled = locationsByDate[formattedDate] ? true : false;
    // 		return [enabled, "", ""];
    // 	}
    // });

    var fileInput = document.getElementById('fileInput');

    fileInput.addEventListener('change', function(e) {
        $("#upload-wrapper").html("<img src='img/loading.gif' style='width:50px; margin: 20px 275px'></img>");

        var file = fileInput.files[0];

        var reader = new FileReader();

        reader.readAsText(file);

        reader.addEventListener('load', function(e) {
            processLocationExport(reader.result);
        });
    });

    var locationsByDate = null;

    function getRandomLocations(locationsByDate, count) {
        var randLocations = [];
        var i = 0;
        var dates = _.keys(locationsByDate);
        while (i < count) {
            var date = dates[Math.floor(Math.random() * dates.length)];
            var dateLocations = getLocationsOnDate(date);

            if (dateLocations.length > 2) {
                var location = dateLocations[Math.floor(Math.random() * dateLocations.length)];
                randLocations.push({
                    date: date,
                    location: location
                });
                i++;
            }
        }
        return randLocations;
    }

    function getMonth(dateString) {
    	return dateString.split("/")[0];
    }

    function processLocationExport(fileContents) {
        locationsByDate = parseLocationJson(fileContents);

        $("#upload-wrapper").hide();

        var imageIndex = 0;
        var imageCount = 10;
        var locations = getRandomLocations(locationsByDate, imageCount);
        //var urls = generateStreetViewUrls(locations, false);
        var flatLocations = _.pluck(locations, "location");

        // making this true filters the flatLocations array, filtering similar locations. 
        // Cannot match dates with locations in this case. urls does not match flatLocations.
        var urls = generateStreetViewUrls(flatLocations, false);

        var questionsHtml = "";

        var questionHtmlTpl = "" +
            "<div class='location-questions' id='q{{INDEX}}'>" +
            "<div class='image-pano' style='position:relative'>" +
                "<img class='location' crossorigin='anonymous' src='{{SRC}}' data-date='{{DATE}}' data-millis='{{MILLIS}}' style='width:600px; height:600px;'></img>" +
                "<img class='play-icon' src='img/play.png' style='position:absolute; top:0px; left:0px; width:100px; margin:250px 250px;'>"+
                "<div class='hyperlapse' style='display:none'></div>" +
            "</div>" +
            "<ol>" +
            "<li class='question'>" +
            "<div class='qtext'> Do you remember this place?</div>" +
            "<div class='qanswers'>" +
            "<input type='radio' name='q{{INDEX}}ans1' value='true'></input><span class='ans-label'> Yes</span>" +
            "<input type='radio' name='q{{INDEX}}ans1' value='false'></input><span class='ans-label'> No</span>" +
            "</div>" +
            "</li>" +
            // "<li class='question'>" +
            // 	"<div class='qtext'> Is this an important place to you?</div>" +
            // 	"<div class='qanswers'>" +
            // 		"<input type='radio' name='q{{INDEX}}ans2' value='true'></input><span class='ans-label'> Yes</span>" +
            // 		"<input type='radio' name='q{{INDEX}}ans2' value='false'></input><span class='ans-label'> No</span>" +
            // 	"</div>" +
            // "</li>" +
            "<li class='question'>" +
            "<div class='qtext'> Do you want to keep this picture?</div>" +
            "<div class='qanswers'>" +
            "<input type='radio' name='q{{INDEX}}ans3' value='true'></input><span class='ans-label'> Yes</span>" +
            "<input type='radio' name='q{{INDEX}}ans3' value='false'></input><span class='ans-label'> No</span>" +
            "</div>" +
            "</li>" +
            "<li class='question' style='display:none'>" +
            "<div class='qtext'> Were you traveling alone?</div>" +
            "<div class='qanswers'>" +
            "<input type='radio' name='q{{INDEX}}ans4' value='true'></input><span class='ans-label'> Yes</span>" +
            "<input type='radio' name='q{{INDEX}}ans4' value='false'></input><span class='ans-label'> No</span>" +
            "</div>" +
            "</li>" +
            "</ol>" +
            "<div style='clear: both;''></div>" +
            "</div>";


        urls.forEach(function(url, i) {
            var questionHtml = questionHtmlTpl
                .replace("{{SRC}}", url)
                .replace(/{{INDEX}}/g, i)
                .replace("{{DATE}}", locations[i].date)
                .replace("{{MILLIS}}", locations[i].location.millis);

            questionsHtml += questionHtml;
        });


        var $resDiv = $("#question-list");
        $resDiv.html(questionsHtml);

        $(".image-pano > img.location").each(function() {
        	var datetime = moment(parseInt($(this).attr("data-millis")));
        	var hour = datetime.hour();
        	var exposure = hour > 12 ? -60 : 40;

        	var month = getMonth($(this).attr("data-date"));
        	var saturation = MONTH_SATURATION[month];

        	$(this).attr("data-saturation", saturation);
        	$(this).attr("data-exposure", exposure);
        	
        	Caman(this, function() {
        		this.saturation(saturation);
        		this.exposure(exposure);
    			this.render();
        	});
        });

        $(".image-pano").click(function() {
            var $img = $(this).find(".location")
            var locations = getLocationsOnDate($img.attr("data-date"));

            $img.hide();
            $img.nextAll(".play-icon").hide();
            $img.nextAll(".hyperlapse").show();

            createHyperlapse(locations, $img.nextAll(".hyperlapse")[0]);
        });

        // $(".image-pano > img").on("mouseover", function() {
        //     $(this.parentNode).append("<img class='play-icon' src='img/play.png' style='position:absolute; top:0px; left:0px; width:100px; margin:250px 250px;'>");
        // });

        // $(".image-pano > img").on("mouseout", function() {
        //     $(this.parentNode).find("img.play-icon").remove();
        // });

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
            $("img.location").show();


            $("#submitButton").visible();
            $("#backButton").visible();
            if (imageIndex < urls.length - 1) {
                $("#q" + imageIndex).hide();
                imageIndex++;
            }
            if (imageIndex == urls.length - 1) {
                $("#nextButton").invisible();
            }
            $("#q" + imageIndex).show();
            console.log("Showing: #q" + imageIndex + " imageIndex: " + imageIndex + " imageCount: " + urls.length);
        });
        $("#backButton").click(function() {
            $("div.hyperlapse > *").remove();

            $("#nextButton").visible();
            $("#submitButton").visible();
            if (imageIndex > 0) {
                imageIndex--;
            }
            if (imageIndex == 0) {
                $("#backButton").invisible();
            }
            $("#q" + (imageIndex + 1)).hide();
            $("#q" + imageIndex).show();
            console.log("Showing: #q" + imageIndex + " imageIndex: " + imageIndex + " imageCount: " + urls.length);

        });
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

            //locationHtml += "Date:" + moment(date).format("MM/DD/YYYY H:mm:ss") + " Lat:" + lat + " Lon:" + lon + "\n";
            uniqLocations.push(location);
        });


        //$("#urlList").html("<pre>"+locationHtml+"</pre>")
        return uniqLocations;
    }

    function generateStreetViewUrls(locations, uniq) {
        var urls = [];
        var uniq = {}

        locations.forEach(function(location, i) {
            // always get up to the first 7 digits after decimal
            var lat = ("" + location.latitude).match(/^(.+\.\d{1,7})/)[1];
            var lon = ("" + location.longitude).match(/^(.+\.\d{1,7})/)[1];

            if (uniq) {
                // location unique to 3 digits after decimal point
                var latSig = ("" + lat).match(/^(.+\.\d{1,3})/)[1];
                var lonSig = ("" + lon).match(/^(.+\.\d{1,3})/)[1];
                var key = latSig + "_" + lonSig;

                if (uniq[key]) {
                    return;
                } else {
                    uniq[key] = true;
                }
            }

            var streetViewUrl = "https://maps.googleapis.com/maps/api/streetview?size=600x600&location=" + lat + "," + lon + "&fov=90&heading=270&pitch=10";
            urls.push(streetViewUrl);

            if (Math.random() > 0.99) console.log("Gen URLs: " + i / locations.length * 100 + "%");
        });

        return urls;
    }

    function parseLocationJson(locJson) {
        var obj = JSON.parse(locJson);
        var results = {};

        obj.locations.forEach(function(location, i) {
            if (i % 1000 == 0) {
                console.log("Parsing: " + i + "/" + obj.locations.length);
            }

            if (location.accuracy > 10) return;

            var lat = location.latitudeE7 * 0.0000001;
            var lon = location.longitudeE7 * 0.0000001;
            //console.log("Latitude: " + lat + "    Longitude: " + lon);

            //time checks
            var timeMs = parseInt(location.timestampMs);
            var date = moment(timeMs).format("MM/DD/YYYY");
            if (!results[date]) {
                results[date] = [];
            }
            results[date].push({
                latitude: lat,
                longitude: lon,
                millis: timeMs
            });

            //if (Math.random() > 0.99) 
        });

        return results;
    };

});
