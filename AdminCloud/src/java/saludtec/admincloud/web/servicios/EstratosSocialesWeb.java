/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package saludtec.admincloud.web.servicios;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import saludtec.admincloud.ejb.crud.EstratosSocialesEjb;
import saludtec.admincloud.ejb.entidades.EstratosSociales;
import saludtec.admincloud.web.utilidades.Sesion;
import saludtec.admincloud.web.utilidades.Calendario;

/**
 *
 * @author saintec
 */
@WebServlet(name = "EstratosSocialesWeb", urlPatterns = {"/estratosSociales/*"})
public class EstratosSocialesWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    EstratosSocialesEjb ejbEstratoSocial;
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

                // <editor-fold defaultstate="collapsed" desc="Servicios soportados por metodo POST">
                case "POST":
                    switch (servicio) {
                        case "/guardar":
                            guardarEstratoSocial(request);
                            listarEstratosSociales(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarEstratoSocial(request);
                            listarEstratosSociales(request).writeJSONString(out);
                            break;

                        case "/eliminar":
                            Integer rsp = eliminarEstratoSocial(request);
                            if (rsp == 200) {
                                listarEstratosSociales(request).writeJSONString(out);
                            } else {
                                response.sendError(400, "Estrato social no eliminado");
                            }
                            break;

                        default:
                            response.sendError(404, "Servicio " + servicio + " no existe");
                            break;
                    }
                    break;
                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc="Servicios soportados por metodo GET">
                case "GET":
                    switch (servicio) {
                        case "/listar":
                            listarEstratosSociales(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerEstratoSocial(request).writeJSONString(out);
                            break;

                        default:
                            response.sendError(404, "Servicio " + servicio + " no existe");
                            break;
                    }
                    break;
                // </editor-fold>

                default:
                    response.sendError(501, "Metodo " + metodo + " no soportado.");
                    break;
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Metodos CRUD tipos de documentos">
    public JSONArray guardarEstratoSocial(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        EstratosSociales estratoSocial = new EstratosSociales();
        estratoSocial.setEstratoSocial(r.getParameter("estratoSocial"));
        estratoSocial.setEstado("activo");
        estratoSocial.setFechaCreacion(fechaActual);
        estratoSocial.setUltimaEdicion(fechaActual);
        estratoSocial.setIdClinica(sesion.clinica(r.getSession()));
        estratoSocial = ejbEstratoSocial.guardar(estratoSocial);
        if (estratoSocial.getEstratoSocial()!= null) {
            obj = new JSONObject();
            obj.put("idEstratoSocial", estratoSocial.getIdEstratoSocial());
            array.add(obj);
        }
        return array;
    }

    public JSONArray editarEstratoSocial(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        EstratosSociales estratoSocial = ejbEstratoSocial.traer(Integer.parseInt(r.getParameter("idEstratoSocial")));
        if (estratoSocial != null) {
            estratoSocial.setEstratoSocial(r.getParameter("estratoSocial"));
            estratoSocial.setUltimaEdicion(fechaActual);
            estratoSocial = ejbEstratoSocial.editar(estratoSocial);
            obj = new JSONObject();
            obj.put("idEstratoSocial", estratoSocial.getIdEstratoSocial());
            array.add(obj);
        }
        return array;
    }

    public Integer eliminarEstratoSocial(HttpServletRequest r) {
        Integer ok = ejbEstratoSocial.eliminar(Integer.parseInt(r.getParameter("idEstratoSocial")));
        return ok;
    }

    public JSONArray listarEstratosSociales(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        List<EstratosSociales> estratoSocials = ejbEstratoSocial.listar(sesion.clinica(r.getSession()));
        if (estratoSocials != null) {
            for (EstratosSociales estratoSocial : estratoSocials) {
                if (estratoSocial.getEstado().equals("activo")) {
                    obj = new JSONObject();
                    obj.put("idEstratoSocial", estratoSocial.getIdEstratoSocial());
                    obj.put("estratoSocial", estratoSocial.getEstratoSocial());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray traerEstratoSocial(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        EstratosSociales estratoSocial = ejbEstratoSocial.traer(Integer.parseInt(r.getParameter("idEstratoSocial")));
        if (estratoSocial != null) {
            obj = new JSONObject();
            obj.put("idEstratoSocial", estratoSocial.getIdEstratoSocial());
            obj.put("estratoSocial", estratoSocial.getEstratoSocial());
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
