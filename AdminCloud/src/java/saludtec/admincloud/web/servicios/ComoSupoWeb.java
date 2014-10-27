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
import saludtec.admincloud.ejb.crud.ComoSupoEjb;
import saludtec.admincloud.ejb.entidades.ComoSupo;
import saludtec.admincloud.web.utilidades.Sesion;
import saludtec.admincloud.web.utilidades.Calendario;

/**
 *
 * @author saintec
 */
@WebServlet(name = "ComoSupoWeb", urlPatterns = {"/comoSupo/*"})
public class ComoSupoWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    ComoSupoEjb ejbComoSupo;
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
                            guardarComoSupo(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarComoSupo(request).writeJSONString(out);
                            break;

                        case "/eliminar":
                            Integer rsp = eliminarComoSupo(request);
                            if (rsp == 200) {
                                listarComoSupo(request).writeJSONString(out);
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
                            listarComoSupo(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerComoSupo(request).writeJSONString(out);
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
    public JSONArray guardarComoSupo(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        ComoSupo comoSupo = new ComoSupo();
        comoSupo.setComoSupo(r.getParameter("comoSupo"));
        comoSupo.setEstado("activo");
        comoSupo.setFechaCreacion(fechaActual);
        comoSupo.setUltimaEdicion(fechaActual);
        comoSupo.setIdClinica(sesion.clinica(r.getSession()));
        comoSupo = ejbComoSupo.guardar(comoSupo);
        if (comoSupo.getIdComoSupo() != null) {
            obj = new JSONObject();
            obj.put("idComoSupo", comoSupo.getIdComoSupo());
            array.add(listarComoSupo(r));
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al guardar referencia.");
            array.add(obj);
        }
        return array;
    }

    public JSONArray editarComoSupo(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        ComoSupo comoSupo = ejbComoSupo.traer(Integer.parseInt(r.getParameter("idComoSupo")));
        if (comoSupo != null) {
            comoSupo.setComoSupo(r.getParameter("comoSupo"));
            comoSupo.setUltimaEdicion(fechaActual);
            comoSupo = ejbComoSupo.editar(comoSupo);
            obj = new JSONObject();
            obj.put("idComoSupo", comoSupo.getIdComoSupo());
            array.add(listarComoSupo(r));
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al editar la referencia.");
            array.add(obj);
        }
        return array;
    }

    public Integer eliminarComoSupo(HttpServletRequest r) {
        Integer ok = ejbComoSupo.eliminar(Integer.parseInt(r.getParameter("idComoSupo")));
        return ok;
    }

    public JSONArray listarComoSupo(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        List<ComoSupo> comoSupos = ejbComoSupo.listar(sesion.clinica(r.getSession()));
        if (comoSupos != null) {
            for (ComoSupo comoSupo : comoSupos) {
                if (comoSupo.getEstado().equals("activo")) {
                    obj = new JSONObject();
                    obj.put("idComoSupo", comoSupo.getIdComoSupo());
                    obj.put("comoSupo", comoSupo.getComoSupo());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray traerComoSupo(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        ComoSupo comoSupo = ejbComoSupo.traer(Integer.parseInt(r.getParameter("idComoSupo")));
        if (comoSupo != null) {
            obj = new JSONObject();
            obj.put("idComoSupo", comoSupo.getIdComoSupo());
            obj.put("comoSupo", comoSupo.getComoSupo());
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
