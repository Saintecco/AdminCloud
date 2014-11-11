/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package saludtec.admincloud.web.servicios;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import saludtec.admincloud.ejb.crud.ClavesCorreccionEjb;
import saludtec.admincloud.ejb.entidades.ClavesCorreccionFactura;
import saludtec.admincloud.web.utilidades.Calendario;
import saludtec.admincloud.web.utilidades.Sesion;

/**
 *
 * @author saintec
 */
@WebServlet(name = "ClavesCorreccionWeb", urlPatterns = {"/clavesCorreccion/*"})
public class ClavesCorreccionWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    ClavesCorreccionEjb ejbClaveCorreccion;
    Sesion sesion = new Sesion();
    Date fechaActual = Calendario.fechaCompleta();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            String servicio = request.getPathInfo();
            String metodo = request.getMethod();
            switch (metodo) {

                case "POST":
                    switch (servicio) {
                        case "/guardar":
                            guardarClaveCorreccion(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarClaveCorreccion(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerClaveCorreccion(request).writeJSONString(out);
                            break;

                        default:
                            response.sendError(404, "Servicio " + servicio + " no existe");
                            break;
                    }
                    break;

                default:
                    response.sendError(501, "Metodo " + metodo + " no soportado");
                    break;
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Metodos CRUD tipos de documentos">
    public JSONArray guardarClaveCorreccion(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        ClavesCorreccionFactura claveCorreccion = new ClavesCorreccionFactura();
        claveCorreccion.setClaveCorreccionFactura(r.getParameter("claveCorreccion"));
        claveCorreccion.setFechaCreacion(fechaActual);
        claveCorreccion.setUltimaEdicion(fechaActual);
        claveCorreccion.setIdClinica(sesion.clinica(r.getSession()));
        claveCorreccion = ejbClaveCorreccion.guardar(claveCorreccion);
        if (claveCorreccion.getIdClaveCorreccionFactura() != null) {
            obj = new JSONObject();
            obj.put("claveCorreccion", "Clave creada con exito");
            array.add(obj);
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al guardar la clave de correccion");
            array.add(obj);
        }
        return array;
    }

    public JSONArray editarClaveCorreccion(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        ClavesCorreccionFactura claveCorreccion = ejbClaveCorreccion.traer(sesion.clinica(r.getSession()));
        if (claveCorreccion != null) {
            claveCorreccion.setClaveCorreccionFactura(r.getParameter("claveCorreccion"));
            claveCorreccion.setUltimaEdicion(fechaActual);
            claveCorreccion = ejbClaveCorreccion.editar(claveCorreccion);
            obj = new JSONObject();
            obj.put("claveCorreccion", "Clave editada con exito");
            array.add(obj);
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al editar la clave de correccion");
            array.add(obj);
        }
        return array;
    }

    public JSONArray traerClaveCorreccion(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        ClavesCorreccionFactura claveCorreccion = ejbClaveCorreccion.traer((r.getParameter("claveCorreccion")), sesion.clinica(r.getSession()));
        if (claveCorreccion != null) {
            obj = new JSONObject();
            obj.put("claveCorreccion", "Acceso ok");
            array.add(obj);
        }
        return array;
    }
    // </editor-fold>

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
