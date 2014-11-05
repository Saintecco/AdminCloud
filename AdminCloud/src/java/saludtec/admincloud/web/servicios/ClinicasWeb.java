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
import saludtec.admincloud.ejb.crud.ClinicasEjb;
import saludtec.admincloud.ejb.entidades.Clinicas;
import saludtec.admincloud.web.utilidades.Calendario;
import saludtec.admincloud.web.utilidades.Sesion;

/**
 *
 * @author saintec
 */
@WebServlet(name = "ClinicasWeb", urlPatterns = {"/clinicas/*"})
public class ClinicasWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    ClinicasEjb ejbClinicas;
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
                            guardarClinica(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarClinica(request).writeJSONString(out);
                            break;

                        default:
                            response.sendError(404, "Servicio " + servicio + " no existe");
                            break;
                    }
                    break;

                case "GET":
                    switch (servicio) {
                        case "/listar":
                            listarClinica(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerClinica(request).writeJSONString(out);
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
    public JSONArray guardarClinica(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Clinicas clinica = new Clinicas();
        clinica.setRazonSocial(r.getParameter("razonSocial"));
        clinica.setNit(r.getParameter("nit"));
        clinica.setTipoRegimen(r.getParameter("tipoRegimen"));
        clinica.setDireccionPrincipal(r.getParameter("direccion"));
        clinica.setTelefonoPrinicipal(r.getParameter("telefonoPrincipal"));
        clinica.setFax(r.getParameter("fax"));
        clinica.setEmailPrincipal(r.getParameter("email"));
        clinica.setActividadEconomica(r.getParameter("actividadEconomica"));
        clinica.setOtros(r.getParameter("otros"));
//        clinica.setOtros(r.getParameter("otros"));
//        clinica.setOtros(r.getParameter("otros"));
//        clinica.setOtros(r.getParameter("otros"));
        clinica.setEstado("activo");
        clinica.setFechaCreacion(fechaActual);
        clinica.setUltimaEdicion(fechaActual);
        clinica = ejbClinicas.guardar(clinica);
        if (clinica.getIdClinica() != null) {
            obj = new JSONObject();
            obj.put("idClinica", clinica.getIdClinica());
            array = listarClinica(r);
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al guardar la clinica.");
            array.add(obj);
        }
        return array;
    }

    public JSONArray editarClinica(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Clinicas clinica = ejbClinicas.traer(sesion.clinica(r.getSession()).getIdClinica());
        if (clinica != null) {
            clinica.setRazonSocial(r.getParameter("razonSocial"));
            clinica.setNit(r.getParameter("nit"));
            clinica.setTipoRegimen(r.getParameter("tipoRegimen"));
            clinica.setDireccionPrincipal(r.getParameter("direccion"));
            clinica.setTelefonoPrinicipal(r.getParameter("telefono"));
            clinica.setFax(r.getParameter("fax"));
            clinica.setEmailPrincipal(r.getParameter("email"));
            clinica.setActividadEconomica(r.getParameter("actividadEconomica"));
            clinica.setOtros(r.getParameter("otros"));
            clinica.setUltimaEdicion(fechaActual);
            clinica = ejbClinicas.editar(clinica);
            obj = new JSONObject();
            obj.put("idClinica", clinica.getIdClinica());
            array = traerClinica(r);
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al editar la clinica.");
            array.add(obj);
        }
        return array;
    }

    public JSONArray listarClinica(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        List<Clinicas> clinicas = ejbClinicas.listar();
        if (clinicas != null) {
            for (Clinicas clinica : clinicas) {
                if (clinica.getEstado().equals("activo")) {
                    obj = new JSONObject();
                    obj.put("idClinica", clinica.getIdClinica());
                    obj.put("razonSocial", clinica.getRazonSocial());
                    obj.put("nit", clinica.getNit());
                    obj.put("tipoRegimen", clinica.getTipoRegimen());
                    obj.put("direccion", clinica.getDireccionPrincipal());
                    obj.put("telefono", clinica.getTelefonoPrinicipal());
                    obj.put("fax", clinica.getFax());
                    obj.put("email", clinica.getEmailPrincipal());
                    obj.put("actividadEconomica", clinica.getActividadEconomica());
                    obj.put("otros", clinica.getOtros());
                    obj.put("limiteSedes", clinica.getLimiteSedes());
                    obj.put("limiteUsuarios", clinica.getLimiteUsuarios());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray traerClinica(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Clinicas clinica = ejbClinicas.traer(sesion.clinica(r.getSession()).getIdClinica());
        if (clinica != null) {
            obj = new JSONObject();
            obj.put("idClinica", clinica.getIdClinica());
            obj.put("razonSocial", clinica.getRazonSocial());
            obj.put("nit", clinica.getNit());
            obj.put("tipoRegimen", clinica.getTipoRegimen());
            obj.put("direccion", clinica.getDireccionPrincipal());
            obj.put("telefono", clinica.getTelefonoPrinicipal());
            obj.put("fax", clinica.getFax());
            obj.put("email", clinica.getEmailPrincipal());
            obj.put("actividadEconomica", clinica.getActividadEconomica());
            obj.put("otros", clinica.getOtros());
            obj.put("limiteSedes", clinica.getLimiteSedes());
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
