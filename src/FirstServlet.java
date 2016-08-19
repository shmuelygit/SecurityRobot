import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import robot.Robot;
import robot.RobotAction;


/**
 * Servlet implementation class FirstServlet
 */
@WebServlet(description = "My First Servlet", urlPatterns = { "/FirstServlet" , "/FirstServlet.do"}, initParams = {@WebInitParam(name="id",value="1"),@WebInitParam(name="name",value="pankaj")})
public class FirstServlet extends HttpServlet {

	public FirstServlet() {
		super();
	}

	//handle get request from user
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//extract parameters from request
		String method = request.getParameter("method");
		String time = request.getParameter("time");
		//call robot to handle the request
		activateRobot(method, time);
		//write response to use
		writeState(response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	//call robot to handle request
	protected void activateRobot(String method, String time){
		try{
			//get RobotAction from string
			RobotAction robotAction = RobotAction.valueOf(method);
			//get the call time as Long
			long callTime = Long.parseLong(time);
			//call robot to handle
			Robot.getInstance().handle(robotAction, callTime);
		}
		catch (Exception e){
		}
	}

	protected void writeState(HttpServletResponse response) throws IOException{
		Map<String, String> options = new LinkedHashMap<>();
//	    options.put("bla", blaVal.toString());
	    String json = new Gson().toJson(options);

	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    response.getWriter().write(json);
	}
}
