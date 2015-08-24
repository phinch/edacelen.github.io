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

    
    
function connectChatServer() {
        webSocket = new WebSocket("ws://localhost:8080/WebApplication3/newEndpoint");
        
        // Called when the connection to the server is opened.
        webSocket.onopen = function(event){
            alert("Connection with server open.");
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
      var batch = [];
      
      for(index = 0; index < content.length; index++) {
          
          text = content[index];
          //console.log(text);
          for(key in text){
              if(key == "accuracy"){
                  // push to server in batches(every 500 points)
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
                      batch[i] = line;
                      i++;
                    } 
              }
              
          }
      }
      
        inversedBatch = "";
        for(index1 = batch.length-1; index1 >= 0; index1--){
            // invserse the list
            inversedBatch += batch[index1];
            inversedBatch += ";";
        }
        console.log(inversedBatch);
        
        
        //send in batch
        webSocket.send(inversedBatch);
        webSocket.onmessage = function(evt){ 
            console.log(evt.data);
            //writeResponse(evt.data);
            // TODO: Front-End processing
        };                
    }
    
    
}


