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
import saludtec.admincloud.ejb.crud.DepartamentosEjb;
import saludtec.admincloud.ejb.entidades.Departamentos;
import saludtec.admincloud.web.utilidades.Sesion;
import saludtec.admincloud.web.utilidades.Calendario;

/**
 *
 * @author saintec
 */
@WebServlet(name = "DepartamentosWeb", urlPatterns = {"/departamentos/*"})
public class DepartamentosWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    DepartamentosEjb ejbDepartamento;
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
                            guardarDepartamento(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarDepartamento(request).writeJSONString(out);
                            break;

                        case "/eliminar":
                            eliminarDepartamento(request).writeJSONString(out);
                            break;

                        default:
                            response.sendError(404, "Servicio " + servicio + " no existe");
                            break;
                    }
                    break;

                case "GET":
                    switch (servicio) {
                        case "/listar":
                            listarDepartamentos(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerTiposDocumentos(request).writeJSONString(out);
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

    // <editor-fold defaultstate="collapsed" desc="Metodos CRUD tipos de departamentos">
    public JSONArray guardarDepartamento(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Departamentos departamento = new Departamentos();
        departamento.setDepartamento(r.getParameter("departamento"));
        departamento.setCodigo(r.getParameter("codigoDepartamento"));
        departamento.setEstado("activo");
        departamento.setFechaCreacion(fechaActual);
        departamento.setUltimaEdicion(fechaActual);
        departamento.setIdClinica(sesion.clinica(r.getSession()));
        Departamentos dpmnt = ejbDepartamento.traer(departamento.getCodigo(), sesion.clinica(r.getSession()));
        if (dpmnt == null || dpmnt.getEstado().equals("inactivo")) {
            departamento = ejbDepartamento.guardar(departamento);
            if (departamento.getIdDepartamento() != null) {
                obj = new JSONObject();
                obj.put("idDepartamento", departamento.getIdDepartamento());
                array = listarDepartamentos(r);
            } else {
                obj = new JSONObject();
                obj.put("error", "Error al guardar departamento");
                array.add(obj);
            }
        } else {
            obj = new JSONObject();
            obj.put("error", "Ya existe un departamento con el codigo " + departamento.getCodigo());
            array.add(obj);
        }
        return array;
    }

    public JSONArray editarDepartamento(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Departamentos departamento = ejbDepartamento.traer(Integer.parseInt(r.getParameter("idDepartamento")));
        if (departamento != null) {
            departamento.setDepartamento(r.getParameter("departamento"));
            departamento.setCodigo(r.getParameter("codigoDepartamento"));
            departamento.setUltimaEdicion(fechaActual);
            Departamentos dpmnt = ejbDepartamento.traer(departamento.getCodigo(), sesion.clinica(r.getSession()));
            if (dpmnt == null || dpmnt.getIdDepartamento() == departamento.getIdDepartamento() || dpmnt.getEstado().equals("inactivo")) {
                departamento = ejbDepartamento.editar(departamento);
                obj = new JSONObject();
                obj.put("idDepartamento", departamento.getIdDepartamento());
                array = listarDepartamentos(r);
            } else {
                obj = new JSONObject();
                obj.put("error", "Ya existe un departamento con el codigo " + departamento.getCodigo());
                array.add(obj);
            }
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al editar departamento");
            array.add(obj);
        }
        return array;
    }

    public JSONArray eliminarDepartamento(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Departamentos departamento = ejbDepartamento.traer(Integer.parseInt(r.getParameter("idDepartamento")));
        if (departamento != null) {
            if (departamento.getPacientesList().size() > 0) {
                obj = new JSONObject();
                obj.put("error", "No se puede eliminar departamento '" + departamento.getDepartamento() + "' porque esta asociado a uno varios pacientes");
                array.add(obj);
            } else {
                departamento.setEstado("inactivo");
                departamento = ejbDepartamento.editar(departamento);
                obj = new JSONObject();
                obj.put("idDepartamento", departamento.getIdDepartamento());
                array = listarDepartamentos(r);
            }
        }
        return array;
    }

    public JSONArray listarDepartamentos(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        List<Departamentos> tiposDocumentos = ejbDepartamento.listar(sesion.clinica(r.getSession()));
        if (tiposDocumentos != null) {
            for (Departamentos departamento : tiposDocumentos) {
                if (departamento.getEstado().equals("activo")) {
                    obj = new JSONObject();
                    obj.put("idDepartamento", departamento.getIdDepartamento());
                    obj.put("departamento", departamento.getDepartamento());
                    obj.put("codigoDepartamento", departamento.getCodigo());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray traerTiposDocumentos(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Departamentos departamento = ejbDepartamento.traer(Integer.parseInt(r.getParameter("idDepartamento")));
        if (departamento != null) {
            obj = new JSONObject();
            obj.put("idDepartamento", departamento.getIdDepartamento());
            obj.put("departamento", departamento.getDepartamento());
            obj.put("codigoDepartamento", departamento.getCodigo());
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
