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
import saludtec.admincloud.ejb.crud.ConsecutivosFacturacionEjb;
import saludtec.admincloud.ejb.entidades.ConsecutivosFacturacion;
import saludtec.admincloud.web.utilidades.Calendario;
import saludtec.admincloud.web.utilidades.Sesion;

/**
 *
 * @author saintec
 */
@WebServlet(name = "ConsecutivosFacturacionWeb", urlPatterns = {"/consecutivosfacturacion/*"})
public class ConsecutivosFacturacionWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    ConsecutivosFacturacionEjb ejbConsecutivoFacturacion;
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
                            guardarConsecutivoFacturacion(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarConsecutivoFacturacion(request).writeJSONString(out);
                            break;

                        case "/eliminar":
                            Integer rsp = eliminarConsecutivoFacturacion(request);
                            if (rsp == 200) {
                                listarConsecutivosFacturacion(request).writeJSONString(out);
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
                            listarConsecutivosFacturacion(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerConsecutivoFacturacion(request).writeJSONString(out);
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
    public JSONArray guardarConsecutivoFacturacion(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        ConsecutivosFacturacion consecutivoFacturacion = new ConsecutivosFacturacion();
        consecutivoFacturacion.setResolucion(r.getParameter("consecutivoFacturacion"));
        consecutivoFacturacion.setFechaResolucion(Calendario.stringFecha(r.getParameter("fechaResolucion")));
        consecutivoFacturacion.setNumeroInicial(Integer.parseInt(r.getParameter("numeroInicial")));
        consecutivoFacturacion.setNumeroFinal(Integer.parseInt(r.getParameter("numeroFinal")));
        consecutivoFacturacion.setNumeroActual(Integer.parseInt(r.getParameter("numeroActual")));
        consecutivoFacturacion.setFechaCreacion(fechaActual);
        consecutivoFacturacion.setUltimaEdicion(fechaActual);
        consecutivoFacturacion.setIdClinica(sesion.clinica(r.getSession()));
        if (consecutivoFacturacion.getNumeroActual() < consecutivoFacturacion.getNumeroInicial()) {
            obj = new JSONObject();
            obj.put("error", "El numero actual no puede ser menor al numero inicial");
            array.add(obj);
        } else if (consecutivoFacturacion.getNumeroActual() > consecutivoFacturacion.getNumeroFinal()) {
            obj = new JSONObject();
            obj.put("error", "El numero actual no puede ser mayor al numero final");
            array.add(obj);
        } else {
            consecutivoFacturacion = ejbConsecutivoFacturacion.guardar(consecutivoFacturacion);
            if (consecutivoFacturacion.getIdInformacionFacturacion() != null) {
                obj = new JSONObject();
                obj.put("idInformacionFacturacion", consecutivoFacturacion.getIdInformacionFacturacion());
                array = listarConsecutivosFacturacion(r);
            } else {
                obj = new JSONObject();
                obj.put("error", "Error al guardar informacion de facturacion");
                array.add(obj);
            }
        }
        return array;
    }

    public JSONArray editarConsecutivoFacturacion(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        ConsecutivosFacturacion consecutivoFacturacion = ejbConsecutivoFacturacion.traer(Integer.parseInt(r.getParameter("idInformacionFacturacion")));
        if (consecutivoFacturacion != null) {
            consecutivoFacturacion.setResolucion(r.getParameter("consecutivoFacturacion"));
            consecutivoFacturacion.setFechaResolucion(Calendario.stringFecha(r.getParameter("fechaResolucion")));
            consecutivoFacturacion.setNumeroInicial(Integer.parseInt(r.getParameter("numeroInicial")));
            consecutivoFacturacion.setNumeroFinal(Integer.parseInt(r.getParameter("numeroFinal")));
            consecutivoFacturacion.setNumeroActual(Integer.parseInt(r.getParameter("numeroActual")));
            consecutivoFacturacion.setUltimaEdicion(fechaActual);
            consecutivoFacturacion = ejbConsecutivoFacturacion.editar(consecutivoFacturacion);
            obj = new JSONObject();
            obj.put("idInformacionFacturacion", consecutivoFacturacion.getIdInformacionFacturacion());
            array = listarConsecutivosFacturacion(r);
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al guardar informacion de facturacion");
            array.add(obj);
        }
        return array;
    }

    public Integer eliminarConsecutivoFacturacion(HttpServletRequest r) {
        Integer ok = ejbConsecutivoFacturacion.eliminar(Integer.parseInt(r.getParameter("idInformacionFacturacion")));
        return ok;
    }

    public JSONArray listarConsecutivosFacturacion(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        List<ConsecutivosFacturacion> consecutivosFacturacion = ejbConsecutivoFacturacion.listar(sesion.clinica(r.getSession()));
        if (consecutivosFacturacion != null) {
            for (ConsecutivosFacturacion consecutivoFacturacion : consecutivosFacturacion) {
                obj = new JSONObject();
                obj.put("idInformacionFacturacion", consecutivoFacturacion.getIdInformacionFacturacion());
                obj.put("resolucion", consecutivoFacturacion.getResolucion());
                obj.put("fechaResolucion", consecutivoFacturacion.getFechaResolucion());
                obj.put("numeroInicial", consecutivoFacturacion.getNumeroInicial());
                obj.put("numeroFinal", consecutivoFacturacion.getNumeroFinal());
                obj.put("numeroActual", consecutivoFacturacion.getNumeroActual());
                obj.put("estado", consecutivoFacturacion.getEstado());
                array.add(obj);
            }
        }
        return array;
    }

    public JSONArray traerConsecutivoFacturacion(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        ConsecutivosFacturacion consecutivoFacturacion = ejbConsecutivoFacturacion.traer(Integer.parseInt(r.getParameter("idInformacionFacturacion")));
        if (consecutivoFacturacion != null) {
            obj = new JSONObject();
            obj.put("idInformacionFacturacion", consecutivoFacturacion.getIdInformacionFacturacion());
            obj.put("resolucion", consecutivoFacturacion.getResolucion());
            obj.put("fechaResolucion", consecutivoFacturacion.getFechaResolucion());
            obj.put("numeroInicial", consecutivoFacturacion.getNumeroInicial());
            obj.put("numeroFinal", consecutivoFacturacion.getNumeroFinal());
            obj.put("numeroActual", consecutivoFacturacion.getNumeroActual());
            obj.put("estado", consecutivoFacturacion.getEstado());
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
