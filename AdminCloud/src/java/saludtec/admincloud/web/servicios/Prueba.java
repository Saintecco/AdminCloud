/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package saludtec.admincloud.web.servicios;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import saludtec.admincloud.web.utilidades.Utils;

/**
 *
 * @author saintec
 */
@WebServlet(name = "Prueba", urlPatterns = {"/Prueba/*"})
public class Prueba extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            Date d = new Date();
            out.println(new Timestamp(d.getTime()));
            Date date = new Timestamp(d.getTime());
//            out.println("<br>");
//            out.println(Utils.fecha());
//            out.println("<br>");
//            out.println(Utils.fecha());
//            out.println("<br>");
//            out.println(Utils.fecha());
//            out.println("<br>");
//            out.println(Utils.fecha());
            out.println("<br>");
            out.println(request.getContextPath());
            out.println("<br>");
            out.println(request.getServletPath());
            out.println("<br>");
            out.println(request.getServletContext());
            out.println("<br>");
            out.println(request.getUserPrincipal());
            out.println("<br>");
            out.println(request.getRequestURI());
            out.println("<br>");
            out.println(request.getRequestURL());
            out.println("<br>");
            out.println(request.getPathInfo());
            out.println("<br>");
            out.println(request.getScheme());
            out.println("<br>");
            out.println(request.getProtocol());
            out.println("<br>");
            out.println(request.getRemoteAddr());
            out.println("<br>");

        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
