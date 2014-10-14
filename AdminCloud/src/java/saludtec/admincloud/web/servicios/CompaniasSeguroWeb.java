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
import saludtec.admincloud.ejb.crud.CompaniasSeguroEjb;
import saludtec.admincloud.ejb.entidades.CompaniasDeSeguros;
import saludtec.admincloud.web.utilidades.Sesion;
import saludtec.admincloud.web.utilidades.Utils;

/**
 *
 * @author saintec
 */
@WebServlet(name = "CompaniasSeguroWeb", urlPatterns = {"/companiasDeSeguros/*"})
public class CompaniasSeguroWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    CompaniasSeguroEjb ejbCompaniaSeguro;
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
                            guardarCompaniaDeSeguro(request);
                            listarCompaniasDeSeguros(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarCompaniaDeSeguro(request);
                            listarCompaniasDeSeguros(request).writeJSONString(out);
                            break;

                        case "/eliminar":
                            Integer rsp = eliminarCompaniaDeSeguro(request);
                            if (rsp == 200) {
                                listarCompaniasDeSeguros(request).writeJSONString(out);
                            } else {
                                response.sendError(400, "Compania de seguro no eliminado");
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
                            listarCompaniasDeSeguros(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerCompaniaDeSeguro(request).writeJSONString(out);
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

    // <editor-fold defaultstate="collapsed" desc="Metodos CRUD tipos de companiaSeguros">
    public JSONArray guardarCompaniaDeSeguro(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        CompaniasDeSeguros companiaSeguro = new CompaniasDeSeguros();
        companiaSeguro.setCompaniaDeSeguro(r.getParameter("companiaSeguro"));
        companiaSeguro.setCodigo(r.getParameter("codigoCompaniaSeguro"));
        companiaSeguro.setEstado("activo");
        companiaSeguro.setFechaCreacion(fechaActual);
        companiaSeguro.setUltimaEdicion(fechaActual);
        companiaSeguro.setIdClinica(sesion.clinica(r.getSession()));
        companiaSeguro = ejbCompaniaSeguro.guardar(companiaSeguro);
        if (companiaSeguro.getIdCompaniaDeSeguro() != null) {
            obj = new JSONObject();
            obj.put("idCompaniaSeguro", companiaSeguro.getIdCompaniaDeSeguro());
            array.add(obj);
        }
        return array;
    }

    public JSONArray editarCompaniaDeSeguro(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        CompaniasDeSeguros companiaSeguro = ejbCompaniaSeguro.traer(Integer.parseInt(r.getParameter("idCompaniaSeguro")));
        if (companiaSeguro != null) {
            companiaSeguro.setCompaniaDeSeguro(r.getParameter("companiaSeguro"));
            companiaSeguro.setCodigo(r.getParameter("codigoCompaniaSeguro"));
            companiaSeguro.setUltimaEdicion(fechaActual);
            companiaSeguro = ejbCompaniaSeguro.editar(companiaSeguro);
            obj = new JSONObject();
            obj.put("idCompaniaSeguro", companiaSeguro.getIdCompaniaDeSeguro());
            array.add(obj);
        }
        return array;
    }

    public Integer eliminarCompaniaDeSeguro(HttpServletRequest r) {
        Integer ok = ejbCompaniaSeguro.eliminar(Integer.parseInt(r.getParameter("idCompaniaSeguro")));
        return ok;
    }

    public JSONArray listarCompaniasDeSeguros(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        List<CompaniasDeSeguros> tiposDocumentos = ejbCompaniaSeguro.listar(sesion.clinica(r.getSession()));
        if (tiposDocumentos != null) {
            for (CompaniasDeSeguros companiaSeguro : tiposDocumentos) {
                if (companiaSeguro.getEstado().equals("activo")) {
                    obj = new JSONObject();
                    obj.put("idCompaniaSeguro", companiaSeguro.getIdCompaniaDeSeguro());
                    obj.put("companiaSeguro", companiaSeguro.getCompaniaDeSeguro());
                    obj.put("codigoCompaniaSeguro", companiaSeguro.getCodigo());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray traerCompaniaDeSeguro(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        CompaniasDeSeguros companiaSeguro = ejbCompaniaSeguro.traer(Integer.parseInt(r.getParameter("idCompaniaSeguro")));
        if (companiaSeguro != null) {
            obj = new JSONObject();
            obj.put("idCompaniaSeguro", companiaSeguro.getIdCompaniaDeSeguro());
            obj.put("companiaSeguro", companiaSeguro.getCompaniaDeSeguro());
            obj.put("codigoCompaniaSeguro", companiaSeguro.getCodigo());
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
