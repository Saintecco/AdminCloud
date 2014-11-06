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
import saludtec.admincloud.ejb.crud.ConveniosEjb;
import saludtec.admincloud.ejb.crud.ProcedimientosEjb;
import saludtec.admincloud.ejb.crud.RelProcedimientosConveniosEjb;
import saludtec.admincloud.ejb.entidades.Convenios;
import saludtec.admincloud.ejb.entidades.Procedimientos;
import saludtec.admincloud.ejb.entidades.RelProcedimientosConvenios;
import saludtec.admincloud.web.utilidades.Calendario;
import saludtec.admincloud.web.utilidades.Sesion;

/**
 *
 * @author saintec
 */
@WebServlet(name = "ProcedimientosWeb", urlPatterns = {"/procedimientos/*"})
public class ProcedimientosWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    ProcedimientosEjb ejbProcedimiento;
    @EJB
    CategoriasProcedimientosEjb ejbCaegoriaProcedimiento;
    @EJB
    ConveniosEjb ejbConvenio;
    @EJB
    RelProcedimientosConveniosEjb ejbProcedimientoConvenio;
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
                            guardarProcedimiento(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarProcedimiento(request).writeJSONString(out);
                            break;

                        case "/eliminar":
                            Integer rsp = eliminarProcedimiento(request);
                            if (rsp == 200) {
                                listarProcedimientos(request).writeJSONString(out);
                            } else {
                                response.sendError(400, "Procedimiento no eliminado");
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
                            listarProcedimientos(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerProcedimiento(request).writeJSONString(out);
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

    // <editor-fold defaultstate="collapsed" desc="Metodos CRUD procedimientos">
    public JSONArray guardarProcedimiento(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Procedimientos procedimiento = new Procedimientos();
        procedimiento.setProcedimiento(r.getParameter("procedimiento"));
        procedimiento.setIdCategoriaProcedimiento(ejbCaegoriaProcedimiento.traer(Integer.parseInt(r.getParameter("idCategoriaProcedimiento"))));
        procedimiento.setRango(Integer.parseInt(r.getParameter("rango")));
        procedimiento.setActoQuirurgico(Integer.parseInt(r.getParameter("actoQuirurjico")));
        procedimiento.setAmbitoRealizacion(Integer.parseInt(r.getParameter("ambitoRealizacion")));
        procedimiento.setFinalidadProcedimiento(Integer.parseInt(r.getParameter("finalidadProcedimiento")));
        procedimiento.setCups(r.getParameter("cups"));
        procedimiento.setValor(Double.parseDouble(r.getParameter("valor")));
        procedimiento.setEditable(r.getParameter("editable"));
        procedimiento.setEstado("activo");
        procedimiento.setFechaCreacion(fechaActual);
        procedimiento.setUltimaEdicion(fechaActual);
        procedimiento.setIdClinica(sesion.clinica(r.getSession()));
        if (ejbProcedimiento.traer(procedimiento.getCups(), sesion.clinica(r.getSession())) == null) {
            procedimiento = ejbProcedimiento.guardar(procedimiento);
            if (procedimiento.getIdProcedimiento() != null) {
                obj = new JSONObject();
                obj.put("idProcedimiento", procedimiento.getIdProcedimiento());
                array = listarProcedimientos(r);
            } else {
                obj = new JSONObject();
                obj.put("error", "Error al agregar procedimiento.");
                array.add(obj);
            }
        } else {
            obj = new JSONObject();
            obj.put("error", "Ya existe un procedimiento con el codigo cups " + procedimiento.getCups());
            array.add(obj);
        }
        return array;
    }

    public JSONArray editarProcedimiento(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Procedimientos procedimiento = ejbProcedimiento.traer(Integer.parseInt(r.getParameter("idProcedimiento")));
        if (procedimiento != null) {
            procedimiento.setProcedimiento(r.getParameter("procedimiento"));
            procedimiento.setIdCategoriaProcedimiento(ejbCaegoriaProcedimiento.traer(Integer.parseInt(r.getParameter("idCategoriaProcedimiento"))));
            procedimiento.setRango(Integer.parseInt(r.getParameter("rango")));
            procedimiento.setActoQuirurgico(Integer.parseInt(r.getParameter("actoQuirurjico")));
            procedimiento.setAmbitoRealizacion(Integer.parseInt(r.getParameter("ambitoRealizacion")));
            procedimiento.setFinalidadProcedimiento(Integer.parseInt(r.getParameter("finalidadProcedimiento")));
            procedimiento.setCups(r.getParameter("cups"));
            procedimiento.setValor(Double.parseDouble(r.getParameter("valor")));
            procedimiento.setEditable(r.getParameter("editable"));
            procedimiento.setFechaCreacion(fechaActual);
            procedimiento.setUltimaEdicion(fechaActual);
            procedimiento.setIdClinica(sesion.clinica(r.getSession()));
            procedimiento = ejbProcedimiento.editar(procedimiento);
            obj = new JSONObject();
            obj.put("idProcedimiento", procedimiento.getIdProcedimiento());
            array = listarProcedimientos(r);
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al editar procedimiento.");
            array.add(obj);
        }
        return array;
    }

    public Integer eliminarProcedimiento(HttpServletRequest r) {
        Integer ok = ejbProcedimiento.eliminar(Integer.parseInt(r.getParameter("idProcedimiento")));
        return ok;
    }

    public JSONArray listarProcedimientos(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        List<Procedimientos> procedimientos = ejbProcedimiento.listar(sesion.clinica(r.getSession()));
        if (procedimientos != null) {
            for (Procedimientos procedimiento : procedimientos) {
                if (procedimiento.getEstado().equals("activo")) {
                    obj = new JSONObject();
                    obj.put("idProcedimiento", procedimiento.getIdProcedimiento());
                    obj.put("procedimiento", procedimiento.getProcedimiento());
                    obj.put("idCategoriaProcedimiento", procedimiento.getIdCategoriaProcedimiento().getIdCategoriaProcedimiento());
                    obj.put("rango", procedimiento.getRango());
                    obj.put("actoQuirurjico", procedimiento.getActoQuirurgico());
                    obj.put("ambitoRealizacion", procedimiento.getAmbitoRealizacion());
                    obj.put("finalidadProcedimiento", procedimiento.getFinalidadProcedimiento());
                    obj.put("cups", procedimiento.getCups());
                    obj.put("valor", procedimiento.getValor());
                    obj.put("editable", procedimiento.getEditable());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray traerProcedimiento(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Procedimientos procedimiento = ejbProcedimiento.traer(Integer.parseInt(r.getParameter("idProcedimiento")));
        if (procedimiento != null) {
            obj = new JSONObject();
            obj.put("idProcedimiento", procedimiento.getIdProcedimiento());
            obj.put("procedimiento", procedimiento.getProcedimiento());
            obj.put("idCategoriaProcedimiento", procedimiento.getIdCategoriaProcedimiento().getIdCategoriaProcedimiento());
            obj.put("rango", procedimiento.getRango());
            obj.put("actoQuirurjico", procedimiento.getActoQuirurgico());
            obj.put("ambitoRealizacion", procedimiento.getAmbitoRealizacion());
            obj.put("finalidadProcedimiento", procedimiento.getFinalidadProcedimiento());
            obj.put("cups", procedimiento.getCups());
            obj.put("valor", procedimiento.getValor());
            obj.put("editable", procedimiento.getEditable());
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
