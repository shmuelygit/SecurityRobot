<!DOCTYPE html>
<html lang="en" style="width: 100%; height: 100%">
<head>
<title>Security Robot</title>
<script type="text/javascript" src="jquery-latest.min.js"></script>
<script type="text/javascript" src="Logic.js"></script>
<link rel="stylesheet" type="text/css" href="style.css">
</head>
<body style="width: 100%; height: 100%">
	<div id="bodyContainer">
		<div id="infoContainer" class="infoText">
			<div id="speedInfo">Speed: 160 km/h</div>
			<div id="distInfo">Dist: 3.42m</div>
		</div>
		<div id="moveButtonsContainer">
			<button id="MoveForwardButton" class="moveButton"
				onclick="clickButton('MoveForward')">Forward</button>
			<br>
			<button id="MoveLeftButton" class="moveButton"
				onclick="clickButton('MoveLeft')">Left</button>
			<button id="MoveRightButton" class="moveButton"
				onclick="clickButton('MoveRight')">Right</button>
			<br>
			<button id="MoveBackwardButton" class="moveButton"
				onclick="clickButton('MoveBackward')">Backward</button>

		</div>
		<div id="videoContainer">
			<img style="-webkit-user-select: none"
				src="http://192.168.42.1:8081/">
		</div>
		<div id="cameraButtonsContainer">
			<button id="CamRotateUp" class="cameraButton"
				onclick="clickButton('CamRotateUp')">CamUp</button>
			<br>
			<button id="CamRotateLeft" class="cameraButton"
				onclick="clickButton('CamRotateLeft')">CamLeft</button>
			<button id="CamRotateRight" class="cameraButton"
				onclick="clickButton('CamRotateRight')">CamRight</button>
			<br>
			<button id="CamRotateDown" class="cameraButton"
				onclick="clickButton('CamRotateDown')">CamDown</button>
			<br> <br> <br>
			<button id="CamRotateInit" class="cameraButton"
				onclick="clickButton('CamRotateInit')">CamInit</button>
		</div>

	</div>
	<div style="clear: both"></div>




</body>
</html>