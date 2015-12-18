$(function() {
    var Caman = require('caman');

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

    var ambiance = null;
    var panoWidth  = 640;
    var panoHeight = 640;

    function createHyperlapse(locations, pano) {
        $(pano).html("<img src='img/loading.gif' style='width:50px; margin:275px 275px'></img>");

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
                    duration: 15000,
                    totalFrames: 150,
                    loop: true,
                    width: panoWidth,
                    height: panoHeight,
                    domain: 'http://maps.googleapis.com',
                    key: 'AIzaSyB6Cjbmooja_wX5fnuajKHNfRCTgwpss1E',
                });

                routeSequence.done(function(player) {
                    $(pano).find("img").hide();
                    $(pano).find("canvas").show();
                    $(pano).show();
                    $(pano.parentNode).find('.location').hide();
                    $(pano.parentNode).find('.loading-gif').hide();
                    player.play();
                    ambiance = new Audio("./audio/CarDriving.mp3");
                    ambiance.play();

                    //when cursor is on rewind video
                    $(pano).on("mousemove", function(event) {
                        var mouseX = parseInt(event.pageX - parseInt($(pano).offset().left));
                        var mouseY = parseInt(event.pageY - parseInt($(pano).offset().top));
                        // console.log( "pageX: " + mouseX + ", pageY: " + mouseY );
                        var imagePos = mouseX / panoWidth;
                        console.log("Frame progress:", imagePos);
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

        var first = locations.shift()
        var last = locations.pop()

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

        setTimeout(function() { 
            getRouteFromDirectionsService(route.request, 5, function(err, response) {
                if (err) {
                    throw Error("Direction Service request failed for: " + JSON.stringify(err));
                }

                results[i] = response;

                if (_.compact(results).length == routes.length) {
                    onFinished();
                }
            });
        }, 100 * i);
    }

    function getRouteFromDirectionsService(request, retry, callback) {
        var directions_service = new google.maps.DirectionsService();

        directions_service.route(request, function(response, status) {
            if (status == google.maps.DirectionsStatus.OK) {
                callback(null, response);
            } else {
                if (retry > 0) {
                    setTimeout(function() { 
                        getRouteFromDirectionsService(request, retry - 1, callback); 
                    }, 100 * (1 /retry));
                } else {
                    console.log(status);
                    callback(status, null);
                }
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


 
    var messages = document.getElementById("messages");

    /**
     * Sends the value of the text input to the server
     */
    function send(){
        var text = document.getElementById("messageinput").value;
        webSocket.send(text);
    }

    function closeSocket(){
        webSocket.close();
    }

    function writeResponse(text){
        messages.innerHTML += "<br/>" + text;
    }

    function onMessage(evt) {
        console.log("received: " + evt.data);
        writeResponse(evt.data);
    }

    // dragdrop
    var dragArea = document.getElementById('file-drop-area');
    dragArea.ondragover = function () { this.className = 'dragging'; return false; };
    dragArea.ondragend = dragArea.ondragleave = function () { this.className = ''; return false; };
    dragArea.ondrop = function (e) {
        this.className = '';
        e.preventDefault();
        loadFile(e.dataTransfer);
    }
    // upload
    var fileInput = document.getElementById('fileInput');
    fileInput.addEventListener('change', function(e) {
        loadFile(fileInput);
    });
    function loadFile(input) {
        var file, fr;

        if (typeof window.FileReader !== 'function') {
          alert("The file API isn't supported on this browser yet.");
          return;
        }

        if (!input) {
          alert("Um, couldn't find the fileinput element.");
        }
        else if (!input.files) {
          alert("This browser doesn't seem to support the `files` property of file inputs.");
        }
        else if (!input.files[0]) {
          alert("Please select a file before clicking 'Load'");
        }
        else {
            // conect to websocket
            connectChatServer();
            file = input.files[0];
            fr = new FileReader();
            fr.onload = receivedText;
            fr.readAsText(file);
        }
    }

    function connectChatServer() {
        // webSocket = new WebSocket("ws://localhost:8080/WebApplication/newEndpoint");
        webSocket = new WebSocket("ws://rewind.cs.brown.edu/app/newEndpoint");

        // Called when the connection to the server is opened.
          
        webSocket.onopen = function(event){
            console.log("ready state is: ");
            console.log(webSocket.readyState);
            console.log("Connection established!");
            // alert("Connection with server open."); 
        };

        // When the server is sending data to this socket, this method is called
        webSocket.onmessage = function(event){
            var data = event.data;
            writeResponse(data);
        };

        // Called when the connection to the server is closed.
        webSocket.onclose = function() {
            alert("Connection is closed...");
        };

        webSocket.onerror = function(event) {
            alert(event.msg);
        }
    }

    function receivedText(e) {     
        lines = e.target.result;
        var line = JSON.parse(lines); 
        var content = line.locations;
        var i = 0;
        var locations = [];

        for(index = 0; index < content.length; index++) {
            text = content[index];
            //console.log(text);
            for(key in text){
                if(key == "accuracy"){
                    if(text.accuracy > 10){
                    // time
                    var timestamp = text.timestampMs;
                    var d = new Date(+timestamp);
                    var formattedDate = d.getFullYear()+ "-" + (d.getMonth() + 1) + "-" + d.getDate();
                    var hours = (d.getHours() < 10) ? "0" + d.getHours() : d.getHours();
                    var minutes = (d.getMinutes() < 10) ? "0" + d.getMinutes() : d.getMinutes();
                    var seconds = (d.getSeconds() < 10) ? "0" + d.getSeconds() : d.getSeconds();
                    var formattedTime = hours + ":" + minutes + ":" + seconds;

                    formattedDate = formattedDate + " " + formattedTime;
                    //document.write(text.timestampMs);
                    //console.log(formattedDate);

                    // latitude
                    var latitude = parseInt(text.latitudeE7,10)/10000000;
                    //console.log(latitude);

                    // longitude
                    var longitude = parseInt(text.longitudeE7,10)/10000000;
                    //console.log(longitude);

                    // put lat, lng, time into an array
                    var line = latitude+","+longitude+","+formattedDate;
                    //console.log(line);
                    locations[i] = line;
                    i++;
                    } 
                }
            }
        }
        invertedLocations = "";
        for(index1 = locations.length-1; index1 >= 0; index1--){
            // invserse the list
            invertedLocations += locations[index1];
            invertedLocations += ";";
        }
        
        
        var lastResponse = null;
        // console.log(invertedLocations);
        try {
            // webSocket.send(invertedLocations);                
            waitForSocketConnection(webSocket, function(){
            console.log("message sent!!!");
            webSocket.send(invertedLocations);
            });    
        } catch (error) {
            console.log(error.toString());
        }
        // webSocket.send(invertedLocations);

        // Called when the connection to the server is closed.
        webSocket.onclose = function() {
            console.log("call");
            handleServerResponse(lastResponse);
            console.log("call~~~~");
        };
        
        webSocket.onmessage = function(evt){ 
            console.log("sent to clinet:", evt.data);
            lastResponse = evt.data;
        }; 
        
    }
    
    // Make the function wait until the connection is made...
    function waitForSocketConnection(socket, callback){
        setTimeout(
                function () {
                    if (socket.readyState === 1) {
                        console.log("Connection is made")
                        if(callback != null){
                            callback();
                        }
                        return;

                    } else {
                        console.log("wait for connection...")
                        waitForSocketConnection(socket, callback);
                    }
                }, 5); // wait 5 milisecond for the connection...
            }

    
    
    
    var processed = false;
    function handleServerResponse(response) {
        // DO SOMETHING WITH THE RESPONSE
        console.log("handleServerResponse!!!!!");
        if (response !== "" && !processed){
            var singleTrip = parseLocationsCsv(response);
            // console.log(singleTrip);
            // generateRewindSurvey(singleTrip);
            processLocationExport(singleTrip);
            processed = true;
        }
        
    }

    function parseLocationsCsv(csv) {
        //var lines = csv.split(/\r?\n/);
        var lines = csv.split(";");
        var results = [];

        lines.forEach(function(line, i) {
            var parts = line.split(",");

            var lat    = parseFloat(parts[0]);
            var lon    = parseFloat(parts[1]);
            var timeMs = parseInt(parts[2]);
            
            results.push({
                latitude: lat,
                longitude: lon,
                millis: timeMs
            });
        });
        //console.log(results.length);
        return results;
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

    var timeZoneUrlTpl = "https://maps.googleapis.com/maps/api/timezone/json?location={{LAT}},{{LON}}&timestamp={{MILLIS}}";

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

        var month = getMonth(datetime.format("MM/DD/YYYY"));
        // var saturation = MONTH_SATURATION[month];
        
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
                $(newImage).attr("data-hour", $(image).attr("data-hour"));
                $(newImage).attr("data-localHour", $(image).attr("data-localHour"));

                return caman.pipeline(function () {
                    this.saturation(saturation);
                    this.exposure(exposure);
                }).then(function () {
                    if (callback)  {
                        callback(parent.childNodes[0]);
                    }
                });
            });
        });
    }



    
    
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

    function processLocationExport(trips) {
        locationsByDate = parseLocationInputFromServer(trips);

        $("#upload-wrapper").hide();

        var imageIndex = 0;
        var imageCount = 10;
        var locations = getRandomLocations(locationsByDate, imageCount);
        // console.log(locations);
        //var urls = generateStreetViewUrls(locations, false);
        var flatLocations = _.pluck(locations, "location");

        // making this true filters the flatLocations array, filtering similar locations. 
        // Cannot match dates with locations in this case. urls does not match flatLocations.
        var urls = generateStreetViewUrls(flatLocations, false);

        var questionsHtml = "";

        var questionHtmlTpl = "" +
            "<div class='location-questions' id='q{{INDEX}}'>" +
            "<div class='image-pano' style='position:relative'>" +
                "<img class='location' crossorigin='anonymous' src='{{SRC}}' data-date='{{DATE}}' data-millis='{{MILLIS}}' data-lat='{{LAT}}' data-lon='{{LON}}' style='width:"+ panoWidth +"px; height:"+ panoHeight +"px;'></img>" +
                "<img class='play-icon' src='img/play.png' style='position:absolute; top:0px; left:0px; width:100px; margin:270px 270px;'>"+
                "<img class='loading-gif' src='img/loading.gif' style='position:absolute; top:0px; left:0px; width:100px; margin:270px 270px;'></img>"+
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
                .replace("{{LAT}}", locations[i].location.latitude)
                .replace("{{LON}}", locations[i].location.longitude)
                .replace("{{MILLIS}}", locations[i].location.millis);

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
            $(this).addClass("loading-hyperlapse");
            var $img = $(this).find(".location")
            var locations = getLocationsOnDate($img.attr("data-date"));
            // var locations = trips[parseInt($img.attr("data-trip"))];

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

            createHyperlapse(locations, $img.nextAll(".hyperlapse")[0]);
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
            if (ambiance) ambiance.pause();

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
            if (ambiance) ambiance.pause();


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
            $("div.hyperlapse").hide().find("*").remove();
            $("img.location, canvas.location").show().parent().removeClass("loading-hyperlapse");
            if (ambiance) ambiance.pause();

            $("#nextButton").visible();
            $("#submitButton").visible();
            if (ambiance) ambiance.pause();
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

            var streetViewUrl = "https://maps.googleapis.com/maps/api/streetview?key=AIzaSyB6Cjbmooja_wX5fnuajKHNfRCTgwpss1E&size="+ panoWidth +"x"+ panoHeight +"&location=" + lat + "," + lon + "&fov=90&heading=270&pitch=10";
            urls.push(streetViewUrl);

            // if (Math.random() > 0.99) console.log("Gen URLs: " + i / locations.length * 100 + "%");
        });

        return urls;
    }

    function parseLocationJson(locJson) {
        var obj = JSON.parse(locJson);
        var results = {};

        obj.locations.forEach(function(location, i) {
            if (i % 1000 == 0) {
                // console.log("Parsing: " + i + "/" + obj.locations.length);
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
    
    function parseLocationInputFromServer(input) {
        // var obj = JSON.parse(locJson);
        var results = {};
        console.log(input);
        // var eachRecord = input.split(";");
        
        input.forEach(function(location, i) {
            var lat = location.latitude;
            var lon = location.longitude;
            var timeMs = parseInt(location.millis);
            var date = moment(timeMs).format("MM/DD/YYYY");
            // console.log(lat);
            // console.log(lon);
            // console.log(timeMs);
            console.log(date);
            if (!results[date]) {
                results[date] = [];
            }
            results[date].push({
                latitude: lat,
                longitude: lon,
                millis: timeMs
            });
        });
        
        return results;
    }; 
    
    
});
