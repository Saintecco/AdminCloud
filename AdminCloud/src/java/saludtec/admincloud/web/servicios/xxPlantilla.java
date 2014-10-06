/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package saludtec.admincloud.web.servicios;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author saintec
 */
@WebServlet(name = "xxPlantilla", urlPatterns = {"/xxPlantilla"})
public class xxPlantilla extends HttpServlet {

    JSONArray arrayJson;
    JSONObject obJson;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {

        }
    }

    // <editor-fold defaultstate="collapsed" desc="Metodos HttpServlet. Click on the + sign on the left to edit the code.">
    //-------------------------------------------------------------------
    //Metodo GET ---->
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String servicio = request.getRequestURI().replaceAll("/AdminCloud/servlet/", "");
            switch (servicio) {
                case "caso":
                    arrayJson = new JSONArray();
                    break;

                default:
                    break;
            }

        } catch (Exception ex) {
            arrayJson = new JSONArray();
            obJson = new JSONObject();
            arrayJson.add(obJson.put("error", ex.getMessage()));
        }
    }

    //Metodo POST ---->
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String servicio = request.getRequestURI().replaceAll("/AdminCloud/servlet/", "");
            switch (servicio) {
                case "caso":
                    arrayJson = new JSONArray();
                    break;

                default:
                    break;
            }
        } catch (Exception ex) {
            arrayJson = new JSONArray();
            obJson = new JSONObject();
            arrayJson.add(obJson.put("error", ex.getMessage()));
        }
    }

    //Metodo PUT ---->
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String servicio = request.getRequestURI().replaceAll("/AdminCloud/servlet/", "");
            switch (servicio) {
                case "caso":
                    arrayJson = new JSONArray();
                    break;

                default:
                    break;
            }
        } catch (Exception ex) {
            arrayJson = new JSONArray();
            obJson = new JSONObject();
            arrayJson.add(obJson.put("error", ex.getMessage()));
        }
    }

    //Metodo DELETE ---->
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String servicio = request.getRequestURI().replaceAll("/AdminCloud/servlet/", "");
            switch (servicio) {
                case "caso":
                    arrayJson = new JSONArray();
                    break;

                default:
                    break;
            }
        } catch (Exception ex) {
            arrayJson = new JSONArray();
            obJson = new JSONObject();
            arrayJson.add(obJson.put("error", ex.getMessage()));
        }
    }

    //Servlet info ---->
    @Override
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>

}
