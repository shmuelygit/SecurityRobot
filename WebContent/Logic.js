/**
 * 
 */

var clickButton = function(userMethod){
	$.get("FirstServlet",
			{ method: userMethod, time:(new Date).getTime()},
			function(responseJson) {
			}
	);
}

var handleState = function(responseJson){
	$.each(responseJson, function(index, item) { // Iterate over the JSON array.
    	document.getElementById(index).innerHTML = item;
    });
}

var keyPressed = {};
var buttons = [ "#MoveForwardButton", 
                "#MoveBackwardButton", 
                "#MoveRightButton",
                "#MoveLeftButton", 
                "#CamRotateRight", 
                "#CamRotateLeft", 
                "#CamRotateUp", 
                "#CamRotateDown",
                "#CamRotateInit"];

var buttonsKeys = [ "38",
                    "40",
                    "39",
                    "37",
                    "68",
                    "65",
                    "87",
                    "83",
                    "9"];

var refreshTimeMillis = 100;

document.addEventListener('keydown', function(e) {
	keyPressed[e.keyCode] = true;
}, false);
document.addEventListener('keyup', function(e) {
	keyPressed[e.keyCode] = false;
}, false);

function robotLoop() {
	for (i = 0; i < buttonsKeys.length; i++) {
		if(keyPressed[buttonsKeys[i]]){
			$(buttons[i]).click();
		}
	}
	setTimeout(robotLoop, refreshTimeMillis);
}

robotLoop();