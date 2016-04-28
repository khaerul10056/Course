import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/AccessoryMgn")
public class AccessoryMgn extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();
        Helper helper = new Helper(request, pw);

        displayAccessory(request, response);
    }

    protected void displayAccessory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();
        Helper helper = new Helper(request,pw);
        if(!helper.isLoggedin()){
            HttpSession session = request.getSession(true);
            session.setAttribute("login_msg", "Please login to manage accessory");
            response.sendRedirect("Login");
            return;
        }
        String usertype = helper.usertype();
        String errmsg = "";
        if (usertype==null || !usertype.equals("storemanager")) {
            errmsg = "You have no authorization to manage accessary!";
        }

        helper.prepareLayout();
        helper.prepareHeader();
        helper.prepareMenu();
        String content = "<section id='content'>";
        content += "  <div class='cart'>";
        content += "    <h3>Accessory List</h3>";
        content += "    <div style='padding:5px'><a href='AccessoryAdd' class='button'>Create New Accessory</a></div>";
        if(errmsg.isEmpty()){
            content += "<table cellspacing='0'>";
            content += "<tr><th>No.</th><th>Console</th><th>Accessory Name</th><th>Price</th><th>Management</th></tr>";
            int i = 1;
            for(Map.Entry<String, Console> entry : helper.getConsoles().entrySet()){
                Console console = entry.getValue();
                for (Map.Entry<String, Accessory> entry2 : console.getAccessories().entrySet()) {
                    Accessory ac = entry2.getValue();
                    content += "<tr>";
                    content += "<td>"+i+"</td><td>"+console.getName()+"</td><td>"+ac.getName()+"</td><td>$"+ac.getPrice()+"</td>";
                    content += "<td>";
                    content += "  <span style='padding-right:3px;'><a href='AccessoryEdit?manufacturer="+console.getManufacturer()+"&console="+console.getKey()+"&accessory="+ac.getKey()+"' class='button'>Edit</a></span>";
                    content += "  <span><a href='AccessoryDel?manufacturer="+console.getManufacturer()+"&console="+console.getKey()+"&accessory="+ac.getKey()+"'' class='button' onclick=\"return confirm('Are you sure you want to delete this accessary?')\">Delete</a></span>";
                    content += "</td>";
                    content += "</tr>";
                    i++;
            }
            }
            content += "</table>";
        } else {
            content += "<h4 style='color:red'>"+errmsg+"</h4>";
        }
        content += "  </div>";
        content += "</section>";
        helper.prepareContent(content);
        helper.prepareSideBar();
        helper.prepareFooter();
        helper.printHtml();
    }
}
