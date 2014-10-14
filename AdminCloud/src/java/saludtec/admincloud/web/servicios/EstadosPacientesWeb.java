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
import saludtec.admincloud.ejb.crud.EstadosPacientesEjb;
import saludtec.admincloud.ejb.entidades.EstadosPacientes;
import saludtec.admincloud.web.utilidades.Sesion;
import saludtec.admincloud.web.utilidades.Utils;

/**
 *
 * @author saintec
 */
@WebServlet(name = "EstadosPacientesWeb", urlPatterns = {"/estadosPacientes/*"})
public class EstadosPacientesWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    EstadosPacientesEjb ejbEstadoPaciente;
    Sesion sesion = new Sesion();
    Date fechaActual = Utils.fechaCompleta();

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
                            guardarEstadoPaciente(request);
                            listarEstadosPacientes(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarEstadoPaciente(request);
                            listarEstadosPacientes(request).writeJSONString(out);
                            break;

                        case "/eliminar":
                            Integer rsp = eliminarEstadoPaciente(request);
                            if (rsp == 200) {
                                listarEstadosPacientes(request).writeJSONString(out);
                            } else {
                                response.sendError(400, "Estado paciente no eliminado");
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
                            listarEstadosPacientes(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerEstadoPaciente(request).writeJSONString(out);
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
    public JSONArray guardarEstadoPaciente(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        EstadosPacientes estadoPaciente = new EstadosPacientes();
        estadoPaciente.setEstadoPaciente(r.getParameter("estadoPaciente"));
        estadoPaciente.setEstado("activo");
        estadoPaciente.setFechaCreacion(fechaActual);
        estadoPaciente.setUltimaEdicion(fechaActual);
        estadoPaciente.setIdClinica(sesion.clinica(r.getSession()));
        estadoPaciente = ejbEstadoPaciente.guardar(estadoPaciente);
        if (estadoPaciente.getIdEstadoPaciente() != null) {
            obj = new JSONObject();
            obj.put("idEstadoPaciente", estadoPaciente.getIdEstadoPaciente());
            array.add(obj);
        }
        return array;
    }

    public JSONArray editarEstadoPaciente(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        EstadosPacientes estadoPaciente = ejbEstadoPaciente.traer(Integer.parseInt(r.getParameter("idEstadoPaciente")));
        if (estadoPaciente != null) {
            estadoPaciente.setEstadoPaciente(r.getParameter("estadoPaciente"));
            estadoPaciente.setUltimaEdicion(fechaActual);
            estadoPaciente = ejbEstadoPaciente.editar(estadoPaciente);
            obj = new JSONObject();
            obj.put("idEstadoPaciente", estadoPaciente.getIdEstadoPaciente());
            array.add(obj);
        }
        return array;
    }

    public Integer eliminarEstadoPaciente(HttpServletRequest r) {
        Integer ok = ejbEstadoPaciente.eliminar(Integer.parseInt(r.getParameter("idEstadoPaciente")));
        return ok;
    }

    public JSONArray listarEstadosPacientes(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        List<EstadosPacientes> estadoPacientes = ejbEstadoPaciente.listar(sesion.clinica(r.getSession()));
        if (estadoPacientes != null) {
            for (EstadosPacientes estadoPaciente : estadoPacientes) {
                if (estadoPaciente.getEstado().equals("activo")) {
                    obj = new JSONObject();
                    obj.put("idEstadoPaciente", estadoPaciente.getIdEstadoPaciente());
                    obj.put("estadoPaciente", estadoPaciente.getEstadoPaciente());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray traerEstadoPaciente(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        EstadosPacientes estadoPaciente = ejbEstadoPaciente.traer(Integer.parseInt(r.getParameter("idEstadoPaciente")));
        if (estadoPaciente != null) {
            obj = new JSONObject();
            obj.put("idEstadoPaciente", estadoPaciente.getIdEstadoPaciente());
            obj.put("estadoPaciente", estadoPaciente.getEstadoPaciente());
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
