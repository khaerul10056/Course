<%@page import="Johnny.Dao.UserDao"%>
<%@page import="Johnny.Beans.User"%>
<%@page import="Johnny.Common.Constants"%>
<%@page import="Johnny.Common.Helper"%>

<jsp:include page="layout_top.jsp" />
<jsp:include page="layout_header.jsp" />
<%
    String errmsg = "";
    String username = "";
    String password = "";
    String repassword = "";
    
    Helper helper = new Helper(request);
    if(helper.isLoggedin()){
        errmsg = "Please logout first!";
    } else {    
        String usertype = Constants.CONST_TYPE_CUSTOMER;
        if ("GET".equalsIgnoreCase(request.getMethod())) {

        } else { 

            username = request.getParameter("username");
            password = request.getParameter("password");
            repassword = request.getParameter("repassword");            
            if(!helper.isLoggedin()) {
                usertype = request.getParameter("usertype");
            }

            if(!password.equals(repassword)){
                errmsg = "Password and Re-Password doesn't match!";
            } else {
                UserDao dao = UserDao.createInstance();
                User user = dao.getUser(username);
                if(user!=null) {
                    errmsg = "User ["+user.getName()+"] already exists as " + user.getUsertype();
                } else {
                    User newuser = new User(username,password,usertype);
                    dao.addUser(newuser);
                    session.setAttribute(helper.SESSION_LOGIN_MSG, "Your "+usertype+" account has been created. Please login");
                    if(!helper.isLoggedin()){
                        response.sendRedirect("account_login.jsp"); return;
                    } else {
                        response.sendRedirect("index.jsp"); return;
                    }
                }
            }
        }
    }
%>
<jsp:include page="layout_menu.jsp" />
<section id="content">
  <div class="post">    
    <h3 style='color:red'><%= errmsg %></h3>    
    <form action="account_register.jsp" method="Post">
      <table style='width:50%'>
          <tr><td><h5>User Name:</h5></td><td><input type='text' name='username' value='<%=username%>' class='input' required /></td></tr>
          <tr><td><h5>Password:</h5></td><td><input type='password' name='password' value='<%=password%>' class='input' required /></td></tr>
        <tr><td><h5>Re-Password:</h5></td><td><input type='password' name='repassword' value='<%=repassword%>' class='input' required /></td></tr>
        <tr><td><h5>User Type</h5></td><td><select name='usertype' class='input'><option value='customer' selected>Customer</option><option value='storemanager'>Store Manager</option><option value='salesman'>Salesman</option></select></td></tr>
        <tr><td colspan="2"><input name="register" class="formbutton" value="Register" type="submit" /></td></tr>
      </table>
    </form>
  </div>
</section>
<jsp:include page="layout_footer.jsp" />