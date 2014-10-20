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
import saludtec.admincloud.ejb.crud.TiposDocumentosEjb;
import saludtec.admincloud.ejb.entidades.TiposDeDocumentos;
import saludtec.admincloud.web.utilidades.Sesion;
import saludtec.admincloud.web.utilidades.Calendario;

/**
 *
 * @author saintec
 */
@WebServlet(name = "TiposDocumentosWeb", urlPatterns = {"/tiposDocumentos/*"})
public class TiposDocumentosWeb extends HttpServlet {

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
                
                // <editor-fold defaultstate="collapsed" desc="Servicios soportados por metodo POST">
                case "POST":
                    switch (servicio) {
                        case "/guardar":
                            guardarTipoDocumento(request);
                            listarTiposDocumentos(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarTipoDocumento(request);
                            listarTiposDocumentos(request).writeJSONString(out);
                            break;

                        case "/eliminar":
                            Integer rsp = eliminarTipoDocumento(request);
                            if (rsp == 200) {
                                listarTiposDocumentos(request).writeJSONString(out);
                            } else {
                                response.sendError(400, "Documento no eliminado");
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
                            listarTiposDocumentos(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerTipoDocumento(request).writeJSONString(out);
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
    public JSONArray guardarTipoDocumento(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        TiposDeDocumentos tipoDocumento = new TiposDeDocumentos();
        tipoDocumento.setTipoDeDocumento(r.getParameter("tipoDocumento"));
        tipoDocumento.setEstado("activo");
        tipoDocumento.setFechaCreacion(fechaActual);
        tipoDocumento.setUltimaEdicion(fechaActual);
        tipoDocumento.setIdClinica(sesion.clinica(r.getSession()));
        tipoDocumento = ejbTipoDocumento.guardar(tipoDocumento);
        if (tipoDocumento.getIdTipoDeDocumento() != null) {
            obj = new JSONObject();
            obj.put("idTipoDocumento", tipoDocumento.getIdTipoDeDocumento());
            array.add(obj);
        }
        return array;
    }

    public JSONArray editarTipoDocumento(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        TiposDeDocumentos tipoDocumento = ejbTipoDocumento.traer(Integer.parseInt(r.getParameter("idTipoDocumento")));
        if (tipoDocumento != null) {
            tipoDocumento.setTipoDeDocumento(r.getParameter("tipoDocumento"));
            tipoDocumento.setUltimaEdicion(fechaActual);
            tipoDocumento = ejbTipoDocumento.editar(tipoDocumento);
            obj = new JSONObject();
            obj.put("idTipoDocumento", tipoDocumento.getIdTipoDeDocumento());
            array.add(obj);
        }
        return array;
    }

    public Integer eliminarTipoDocumento(HttpServletRequest r) {
        Integer ok = ejbTipoDocumento.eliminar(Integer.parseInt(r.getParameter("idTipoDocumento")));
        return ok;
    }

    public JSONArray listarTiposDocumentos(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        List<TiposDeDocumentos> tiposDocumentos = ejbTipoDocumento.listar(sesion.clinica(r.getSession()));
        if (tiposDocumentos != null) {
            for (TiposDeDocumentos tipoDocumento : tiposDocumentos) {
                if (tipoDocumento.getEstado().equals("activo")) {
                    obj = new JSONObject();
                    obj.put("idTipoDocumento", tipoDocumento.getIdTipoDeDocumento());
                    obj.put("tipoDocumento", tipoDocumento.getTipoDeDocumento());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray traerTipoDocumento(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        TiposDeDocumentos tipoDocumento = ejbTipoDocumento.traer(Integer.parseInt(r.getParameter("idTipoDocumento")));
        if (tipoDocumento != null) {
            obj = new JSONObject();
            obj.put("idTipoDocumento", tipoDocumento.getIdTipoDeDocumento());
            obj.put("tipoDocumento", tipoDocumento.getTipoDeDocumento());
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
