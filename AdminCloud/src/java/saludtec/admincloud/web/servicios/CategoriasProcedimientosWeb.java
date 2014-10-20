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
import saludtec.admincloud.ejb.crud.CategoriasProcedimientosEjb;
import saludtec.admincloud.ejb.entidades.CategoriasProcedimientos;
import saludtec.admincloud.web.utilidades.Calendario;
import saludtec.admincloud.web.utilidades.Sesion;

/**
 *
 * @author saintec
 */
@WebServlet(name = "CategoriasProcedimientosWeb", urlPatterns = {"/categoriasProcedimientos/*"})
public class CategoriasProcedimientosWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    CategoriasProcedimientosEjb ejbCategoriaProcedimiento;
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
                            guardarCategoriaProcedimiento(request);
                            listarCategoriasProcedimientos(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarCategoriaProcedimiento(request);
                            listarCategoriasProcedimientos(request).writeJSONString(out);
                            break;

                        case "/eliminar":
                            Integer rsp = eliminarCategoriaProcedimiento(request);
                            if (rsp == 200) {
                                listarCategoriasProcedimientos(request).writeJSONString(out);
                            } else {
                                response.sendError(400, "Categoria procedimiento no eliminado");
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
                            listarCategoriasProcedimientos(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerCategoriaProcedimiento(request).writeJSONString(out);
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
    public JSONArray guardarCategoriaProcedimiento(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        CategoriasProcedimientos categoriaProcedimiento = new CategoriasProcedimientos();
        categoriaProcedimiento.setCategoriaProcedimiento(r.getParameter("categoriaProcedimiento"));
        categoriaProcedimiento.setEstado("activo");
        categoriaProcedimiento.setFechaCreacion(fechaActual);
        categoriaProcedimiento.setUltimaEdicion(fechaActual);
        categoriaProcedimiento.setIdClinica(sesion.clinica(r.getSession()));
        categoriaProcedimiento = ejbCategoriaProcedimiento.guardar(categoriaProcedimiento);
        if (categoriaProcedimiento.getIdCategoriaProcedimiento() != null) {
            obj = new JSONObject();
            obj.put("idCategoriaProcedimiento", categoriaProcedimiento.getIdCategoriaProcedimiento());
            array.add(obj);
        }
        return array;
    }

    public JSONArray editarCategoriaProcedimiento(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        CategoriasProcedimientos categoriaProcedimiento = ejbCategoriaProcedimiento.traer(Integer.parseInt(r.getParameter("idCategoriaProcedimiento")));
        if (categoriaProcedimiento != null) {
            categoriaProcedimiento.setCategoriaProcedimiento(r.getParameter("categoriaProcedimiento"));
            categoriaProcedimiento.setUltimaEdicion(fechaActual);
            categoriaProcedimiento = ejbCategoriaProcedimiento.editar(categoriaProcedimiento);
            obj = new JSONObject();
            obj.put("IdCategoriaProcedimiento", categoriaProcedimiento.getIdCategoriaProcedimiento());
            array.add(obj);
        }
        return array;
    }

    public Integer eliminarCategoriaProcedimiento(HttpServletRequest r) {
        Integer ok = ejbCategoriaProcedimiento.eliminar(Integer.parseInt(r.getParameter("idCategoriaProcedimiento")));
        return ok;
    }

    public JSONArray listarCategoriasProcedimientos(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        List<CategoriasProcedimientos> categoriaProcedimientos = ejbCategoriaProcedimiento.listar(sesion.clinica(r.getSession()));
        if (categoriaProcedimientos != null) {
            for (CategoriasProcedimientos categoriaProcedimiento : categoriaProcedimientos) {
                if (categoriaProcedimiento.getEstado().equals("activo")) {
                    obj = new JSONObject();
                    obj.put("idCategoriaProcedimiento", categoriaProcedimiento.getIdCategoriaProcedimiento());
                    obj.put("categoriaProcedimiento", categoriaProcedimiento.getCategoriaProcedimiento());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray traerCategoriaProcedimiento(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        CategoriasProcedimientos categoriaProcedimiento = ejbCategoriaProcedimiento.traer(Integer.parseInt(r.getParameter("idCategoriaProcedimiento")));
        if (categoriaProcedimiento != null) {
            obj = new JSONObject();
            obj.put("idCategoriaProcedimiento", categoriaProcedimiento.getIdCategoriaProcedimiento());
            obj.put("categoriaProcedimiento", categoriaProcedimiento.getCategoriaProcedimiento());
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
