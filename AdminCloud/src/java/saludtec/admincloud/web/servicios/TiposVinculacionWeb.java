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
import saludtec.admincloud.ejb.crud.TiposVinculacionEjb;
import saludtec.admincloud.ejb.entidades.TiposDeVinculacion;
import saludtec.admincloud.web.utilidades.Sesion;
import saludtec.admincloud.web.utilidades.Calendario;

/**
 *
 * @author saintec
 */
@WebServlet(name = "TiposVinculacionWeb", urlPatterns = {"/tiposVinculacion/*"})
public class TiposVinculacionWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    TiposVinculacionEjb ejbTipoVinculacion;
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
                //Servicios metodo POST
                case "POST":
                    switch (servicio) {
                        case "/guardar":
                            guardarTipoVinculacion(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarTipoVinculacion(request).writeJSONString(out);
                            break;

                        case "/eliminar":
                            eliminarTipoVinculacion(request).writeJSONString(out);
                            break;

                        default:
                            response.sendError(404, "Servicio " + servicio + " no existe");
                            break;
                    }
                    break;
                //Servicios metodo GET
                case "GET":
                    switch (servicio) {
                        case "/listar":
                            listarTiposVinculacion(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerTipoVinculacion(request).writeJSONString(out);
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

    // <editor-fold defaultstate="collapsed" desc="Metodos CRUD tipos de vinculacion">
    public JSONArray guardarTipoVinculacion(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        TiposDeVinculacion tipoVinculacion = new TiposDeVinculacion();
        tipoVinculacion.setTipoDeVinculacion(r.getParameter("tipoVinculacion"));
        tipoVinculacion.setEstado("activo");
        tipoVinculacion.setFechaCreacion(fechaActual);
        tipoVinculacion.setUltimaEdicion(fechaActual);
        tipoVinculacion.setIdClinica(sesion.clinica(r.getSession()));
        tipoVinculacion = ejbTipoVinculacion.guardar(tipoVinculacion);
        if (tipoVinculacion.getIdTipoDeVinculacion() != null) {
            obj = new JSONObject();
            obj.put("idTipoVinculacion", tipoVinculacion.getIdTipoDeVinculacion());
            array = listarTiposVinculacion(r);
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al guardar el tipo de vinculacion");
            array.add(obj);
        }
        return array;
    }

    public JSONArray editarTipoVinculacion(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        TiposDeVinculacion tipoVinculacion = ejbTipoVinculacion.traer(Integer.parseInt(r.getParameter("idTipoVinculacion")));
        if (tipoVinculacion != null) {
            tipoVinculacion.setTipoDeVinculacion(r.getParameter("tipoVinculacion"));
            tipoVinculacion.setUltimaEdicion(fechaActual);
            tipoVinculacion = ejbTipoVinculacion.editar(tipoVinculacion);
            obj = new JSONObject();
            obj.put("idTipoVinculacion", tipoVinculacion.getIdTipoDeVinculacion());
            array = listarTiposVinculacion(r);
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al editar el tipo de vinculacion");
            array.add(obj);
        }
        return array;
    }

    public JSONArray eliminarTipoVinculacion(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        TiposDeVinculacion tipoVinculacion = ejbTipoVinculacion.traer(Integer.parseInt(r.getParameter("idTipoVinculacion")));
        if (tipoVinculacion != null) {
            if (tipoVinculacion.getPacientesList().size() > 0) {
                obj = new JSONObject();
                obj.put("error", "No se puede eliminar tipo de vinculacion '" + tipoVinculacion.getTipoDeVinculacion() + "' porque esta asociado a uno varios pacientes");
                array.add(obj);
            } else {
                tipoVinculacion.setEstado("inactivo");
                tipoVinculacion = ejbTipoVinculacion.editar(tipoVinculacion);
                obj = new JSONObject();
                obj.put("idTipoVinculacion", tipoVinculacion.getIdTipoDeVinculacion());
                array = listarTiposVinculacion(r);
            }
        }
        return array;
    }

    public JSONArray listarTiposVinculacion(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        List<TiposDeVinculacion> tiposDocumentos = ejbTipoVinculacion.listar(sesion.clinica(r.getSession()));
        if (tiposDocumentos != null) {
            for (TiposDeVinculacion tipoVinculacion : tiposDocumentos) {
                if (tipoVinculacion.getEstado().equals("activo")) {
                    obj = new JSONObject();
                    obj.put("idTipoVinculacion", tipoVinculacion.getIdTipoDeVinculacion());
                    obj.put("tipoVinculacion", tipoVinculacion.getTipoDeVinculacion());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray traerTipoVinculacion(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        TiposDeVinculacion tipoVinculacion = ejbTipoVinculacion.traer(Integer.parseInt(r.getParameter("idTipoVinculacion")));
        if (tipoVinculacion != null) {
            obj = new JSONObject();
            obj.put("idTipoVinculacion", tipoVinculacion.getIdTipoDeVinculacion());
            obj.put("tipoVinculacion", tipoVinculacion.getTipoDeVinculacion());
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
