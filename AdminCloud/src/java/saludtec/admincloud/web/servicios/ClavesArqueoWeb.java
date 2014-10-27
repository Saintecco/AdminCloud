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
import saludtec.admincloud.ejb.crud.ClavesArqueoEjb;
import saludtec.admincloud.ejb.entidades.ClavesArqueoDeCaja;
import saludtec.admincloud.web.utilidades.Calendario;
import saludtec.admincloud.web.utilidades.Sesion;

/**
 *
 * @author saintec
 */
@WebServlet(name = "ClavesArqueoWeb", urlPatterns = {"/clavesArqueo/*"})
public class ClavesArqueoWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    ClavesArqueoEjb ejbClaveArqueo;
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
                            guardarClaveArqueo(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarClaveArqueo(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerClaveArqueo(request).writeJSONString(out);
                            break;

                        default:
                            response.sendError(404, "Servicio " + servicio + " no existe");
                            break;
                    }
                    break;

                default:
                    response.sendError(501, "Metodo " + metodo + " no soportado.");
                    break;
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Metodos CRUD tipos de documentos">
    public JSONArray guardarClaveArqueo(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        ClavesArqueoDeCaja claveArqueo = new ClavesArqueoDeCaja();
        claveArqueo.setClaveArqueoDeCaja(r.getParameter("claveArqueo"));
        claveArqueo.setFechaCreacion(fechaActual);
        claveArqueo.setUltimaEdicion(fechaActual);
        claveArqueo.setIdClinica(sesion.clinica(r.getSession()));
        claveArqueo = ejbClaveArqueo.guardar(claveArqueo);
        if (claveArqueo.getIdClaveArqueoDeCaja() != null) {
            obj = new JSONObject();
            obj.put("claveArqueo", "Clave creada con exito");
            array.add(obj);
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al guardar la clave de arqueo.");
            array.add(obj);
        }
        return array;
    }

    public JSONArray editarClaveArqueo(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        ClavesArqueoDeCaja claveArqueo = ejbClaveArqueo.traer(sesion.clinica(r.getSession()));
        if (claveArqueo != null) {
            claveArqueo.setClaveArqueoDeCaja(r.getParameter("claveArqueo"));
            claveArqueo.setUltimaEdicion(fechaActual);
            claveArqueo = ejbClaveArqueo.editar(claveArqueo);
            obj = new JSONObject();
            obj.put("claveArqueo", "Clave editada con exito");
            array.add(obj);
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al editar la clave de arqueo.");
            array.add(obj);
        }
        return array;
    }

    public JSONArray traerClaveArqueo(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        ClavesArqueoDeCaja claveArqueo = ejbClaveArqueo.traer((r.getParameter("claveArqueo")), sesion.clinica(r.getSession()));
        if (claveArqueo != null) {
            obj = new JSONObject();
            obj.put("claveArqueo", "Acceso ok");
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
