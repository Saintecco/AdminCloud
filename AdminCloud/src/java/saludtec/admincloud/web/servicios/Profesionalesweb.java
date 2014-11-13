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
import saludtec.admincloud.ejb.crud.ProfesionalesEjb;
import saludtec.admincloud.ejb.crud.TiposDocumentosEjb;
import saludtec.admincloud.ejb.entidades.Profesionales;
import saludtec.admincloud.ejb.entidades.TiposDeDocumentos;
import saludtec.admincloud.web.utilidades.Calendario;
import saludtec.admincloud.web.utilidades.Sesion;

/**
 *
 * @author saintec
 */
@WebServlet(name = "Profesionalesweb", urlPatterns = {"/profesionales/*"})
public class Profesionalesweb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    ProfesionalesEjb ejbProfesional;
    @EJB
    TiposDocumentosEjb ejbTipoDocumento;
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
                            guardarProfesional(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarProfesional(request).writeJSONString(out);
                            break;

                        case "/eliminar":
                            Integer rsp = eliminarProfesional(request);
                            if (rsp == 200) {
                                listarProfesionales(request).writeJSONString(out);
                            } else {
                                response.sendError(400, "Documento no eliminado");
                            }
                            break;

                        default:
                            response.sendError(404, "Servicio " + servicio + " no existe");
                            break;
                    }
                    break;

                case "GET":
                    switch (servicio) {
                        case "/listar":
                            listarProfesionales(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerProfesional(request).writeJSONString(out);
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

    // <editor-fold defaultstate="collapsed" desc="Metodos CRUD departamentos">
    public JSONArray guardarProfesional(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Profesionales profesional = new Profesionales();
        profesional.setNombre(r.getParameter("nombre"));
        profesional.setApellido(r.getParameter("apellido"));
        profesional.setIdTipoDeDocumento(ejbTipoDocumento.traer(Integer.parseInt(r.getParameter("idTipoDocumento"))));
        profesional.setNumeroDeDocumento(r.getParameter("numeroDocumento"));
        profesional.setTelefono(r.getParameter("telefono"));
        profesional.setEmail(r.getParameter("email"));
        profesional.setUsuario(r.getParameter("usuario"));
        profesional.setEstado(r.getParameter("estado"));
        profesional.setFechaCreacion(fechaActual);
        profesional.setUltimaEdicion(fechaActual);
        profesional.setIdClinica(sesion.clinica(r.getSession()));
        Profesionales pfrs = ejbProfesional.traer(profesional.getNumeroDeDocumento(), sesion.clinica(r.getSession()));
        if (pfrs == null) {
            profesional = ejbProfesional.guardar(profesional);
            if (profesional.getIdTipoDeDocumento() != null) {
                obj = new JSONObject();
                obj.put("idTipoDocumento", profesional.getIdTipoDeDocumento());
                array = listarProfesionales(r);
            } else {
                obj = new JSONObject();
                obj.put("error", "Error al guardar tipo de documento");
                array.add(obj);
            }
        } else {
            obj = new JSONObject();
            obj.put("error", "Ya existe un profesional con el codigo " + profesional.getNumeroDeDocumento());
            array.add(obj);
        }
        return array;
    }

    public JSONArray editarProfesional(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Profesionales profesional = ejbProfesional.traer(Integer.parseInt(r.getParameter("idProfesional")));
        if (profesional != null) {
            profesional.setNombre(r.getParameter("nombre"));
            profesional.setApellido(r.getParameter("apellido"));
            profesional.setIdTipoDeDocumento(ejbTipoDocumento.traer(Integer.parseInt(r.getParameter("idTipoDocumento"))));
            profesional.setNumeroDeDocumento(r.getParameter("numeroDocumento"));
            profesional.setTelefono(r.getParameter("telefono"));
            profesional.setEmail(r.getParameter("email"));
            profesional.setUsuario(r.getParameter("usuario"));
            profesional.setEstado(r.getParameter("estado"));
            profesional.setUltimaEdicion(fechaActual);
            Profesionales pfrs = ejbProfesional.traer(profesional.getNumeroDeDocumento(), sesion.clinica(r.getSession()));
            if (pfrs == null || pfrs.getIdProfesional() == profesional.getIdProfesional()) {
                profesional = ejbProfesional.editar(profesional);
                obj = new JSONObject();
                obj.put("idTipoDocumento", profesional.getIdTipoDeDocumento());
                array = listarProfesionales(r);
            } else {
                obj = new JSONObject();
                obj.put("error", "Ya existe un profesional con el codigo " + profesional.getNumeroDeDocumento());
                array.add(obj);
            }
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al editar profesional");
            array.add(obj);
        }
        return array;
    }

    public Integer eliminarProfesional(HttpServletRequest r) {
        Integer ok = ejbProfesional.eliminar(Integer.parseInt(r.getParameter("idProfesional")));
        return ok;
    }

    public JSONArray listarProfesionales(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        List<Profesionales> profesionales = ejbProfesional.listar(sesion.clinica(r.getSession()));
        if (profesionales != null) {
            for (Profesionales profesional : profesionales) {
                if (profesional.getEstado().equals("activo")) {
                    obj = new JSONObject();
                    obj.put("idProfesional", profesional.getIdProfesional());
                    obj.put("nombre", profesional.getNombre());
                    obj.put("apellido", profesional.getApellido());
                    obj.put("idTipoDocumento", profesional.getIdTipoDeDocumento().getIdTipoDeDocumento());
                    obj.put("numeroDocumento", profesional.getNumeroDeDocumento());
                    obj.put("telefono", profesional.getTelefono());
                    obj.put("email", profesional.getEmail());
                    obj.put("estado", profesional.getEstado());
                    obj.put("usuario", profesional.getUsuario());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray traerProfesional(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Profesionales profesional = ejbProfesional.traer(Integer.parseInt(r.getParameter("idProfesional")));
        if (profesional != null) {
            obj = new JSONObject();
            obj.put("idProfesional", profesional.getIdProfesional());
            obj.put("nombre", profesional.getNombre());
            obj.put("apellido", profesional.getApellido());
            obj.put("idTipoDocumento", profesional.getIdTipoDeDocumento().getIdTipoDeDocumento());
            obj.put("numeroDocumento", profesional.getNumeroDeDocumento());
            obj.put("telefono", profesional.getTelefono());
            obj.put("email", profesional.getEmail());
            obj.put("estado", profesional.getEstado());
            obj.put("usuario", profesional.getUsuario());
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
