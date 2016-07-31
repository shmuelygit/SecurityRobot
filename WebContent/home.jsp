<!DOCTYPE html>
<html lang="en">
<head>
<title>SO question 4112686</title>
<script src="http://code.jquery.com/jquery-latest.min.js"></script>
<script type="text/javascript" src="Logic.js"></script>
</head>
<body>
	<button id="MoveForwardButton" onclick="clickButton('MoveForward')">Forward</button>
	<button id="MoveBackwardButton" onclick="clickButton('MoveBackward')">Backward</button>
	<button id="MoveRightButton" onclick="clickButton('MoveRight')">Right</button>
	<button id="MoveLeftButton" onclick="clickButton('MoveLeft')">Left</button>
	<br>
	<button id="CamRotateRight" onclick="clickButton('CamRotateRight')">CamRight</button>
	<button id="CamRotateLeft" onclick="clickButton('CamRotateLeft')">CamLeft</button>
	<br>
	<img style="-webkit-user-select: none" src="http://192.168.42.1:8081/">

	<br>


	<p>
		onkeypress - <span id="demo"></span>
	</p>
	
</body>
</html>