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
import saludtec.admincloud.ejb.crud.ComisionesEjb;
import saludtec.admincloud.ejb.crud.ProcedimientosEjb;
import saludtec.admincloud.ejb.crud.ProfesionalesEjb;
import saludtec.admincloud.ejb.entidades.Comisiones;
import saludtec.admincloud.ejb.entidades.Procedimientos;
import saludtec.admincloud.ejb.entidades.Profesionales;
import saludtec.admincloud.web.utilidades.Calendario;
import saludtec.admincloud.web.utilidades.Sesion;

/**
 *
 * @author saintec
 */
@WebServlet(name = "ComisionesWeb", urlPatterns = {"/comisiones/*"})
public class ComisionesWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    ComisionesEjb ejbComision;
    @EJB
    ProfesionalesEjb ejbProfesional;
    @EJB
    ProcedimientosEjb ejbProcedimiento;
    @EJB
    CategoriasProcedimientosEjb ejbCategoriaProcedimiento;
    Sesion sesion = new Sesion();
    Date fechaActual = Calendario.fechaCompleta();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            String servicio = request.getPathInfo();
            String metodo = request.getMethod();
            switch (metodo) {

                case "POST":
                    switch (servicio) {
                        case "/guardarProcedimiento":
                            guardarProcedimiento(request).writeJSONString(out);
                            break;

                        case "/guardarProcedimientos":
                            guardarProcedimientos(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarComision(request).writeJSONString(out);
                            break;

                        case "/eliminar":
                            Integer rsp = eliminarComision(request);
                            if (rsp == 200) {
                                listarComisiones(request).writeJSONString(out);
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
                            listarComisiones(request).writeJSONString(out);
                            break;

                        case "/listarProcedimientos":
                            listarProcedimientos(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerComision(request).writeJSONString(out);
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

    // <editor-fold defaultstate="collapsed" desc="Metodos CRUD comisiones">
    public JSONArray guardarProcedimiento(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Profesionales profesional = ejbProfesional.traer(Integer.parseInt(r.getParameter("idProfesional")));
        Procedimientos procedimiento = ejbProcedimiento.traer(Integer.parseInt(r.getParameter("idProcedimiento")));
        Comisiones comision = new Comisiones();
        comision.setIdProfesional(profesional);
        comision.setIdProcedimiento(procedimiento);
        comision.setTipoComision(r.getParameter("tipoComision"));
        comision.setValorComision(Double.parseDouble(r.getParameter("valorComision")));
        comision.setTotal(Double.parseDouble(r.getParameter("total")));
        comision.setEstado("activo");
        comision.setFechaCreacion(fechaActual);
        comision.setUltimaEdicion(fechaActual);
        comision.setIdClinica(sesion.clinica(r.getSession()));
        if (comision.getTipoComision().equals("procentual")) {
            if (procedimiento.getValor() < ((comision.getValorComision() * 100) / procedimiento.getValor())) {
                comision.setTotal(procedimiento.getValor());
            }
        } else {
            if (procedimiento.getValor() < comision.getValorComision()) {
                comision.setTotal(procedimiento.getValor());
            }
        }
        Comisiones cmsn = ejbComision.traer(profesional, procedimiento);
        if (cmsn == null) {
            comision = ejbComision.guardar(comision);
            if (comision.getIdComision() != null) {
                obj = new JSONObject();
                obj.put("idComision", comision.getIdComision());
                array = listarProcedimientos(r);
            } else {
                obj = new JSONObject();
                obj.put("error", "Error al guardar comision");
                array.add(obj);
            }
        } else if (cmsn.getEstado().equals("activo")) {
            obj = new JSONObject();
            obj.put("error", "El procedimiento " + procedimiento.getProcedimiento() + " " + procedimiento.getCups() + " ya esta asociado a este profesional");
            array.add(obj);
        } else if (cmsn.getEstado().equals("inactivo")) {
            comision.setFechaCreacion(cmsn.getFechaCreacion());
            comision = ejbComision.editar(comision);
            if (comision.getIdComision() != null) {
                obj = new JSONObject();
                obj.put("idComision", comision.getIdComision());
                array = listarProcedimientos(r);
            } else {
                obj = new JSONObject();
                obj.put("error", "Error al guardar comision");
                array.add(obj);
            }
        }
        return array;
    }

    public JSONArray guardarProcedimientos(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Profesionales profesional = ejbProfesional.traer(Integer.parseInt(r.getParameter("idProfesional")));
        Integer idCategoriaProcedimiento = Integer.parseInt(r.getParameter("idCategoriaProcedimiento"));
        String tipoComision = r.getParameter("tipoComision");
        Double valorComision = Double.parseDouble(r.getParameter("valorComision"));
        Double total = Double.parseDouble(r.getParameter("total"));
        List<Procedimientos> procedimientos;
        if (idCategoriaProcedimiento == 0) {
            procedimientos = ejbProcedimiento.listar(sesion.clinica(r.getSession()));
        } else {
            procedimientos = ejbProcedimiento.listar(ejbCategoriaProcedimiento.traer(idCategoriaProcedimiento));
        }
        for (Procedimientos procedimiento : procedimientos) {
            Comisiones comision = new Comisiones();
            comision.setIdProfesional(profesional);
            comision.setIdProcedimiento(procedimiento);
            comision.setTipoComision(tipoComision);
            comision.setValorComision(valorComision);
            comision.setTotal(total);
            comision.setEstado("activo");
            comision.setFechaCreacion(fechaActual);
            comision.setUltimaEdicion(fechaActual);
            comision.setIdClinica(sesion.clinica(r.getSession()));
            if (comision.getTipoComision().equals("procentual")) {
                if (procedimiento.getValor() < ((comision.getValorComision() * 100) / procedimiento.getValor())) {
                    comision.setTotal(procedimiento.getValor());
                }
            } else {
                if (procedimiento.getValor() < comision.getValorComision()) {
                    comision.setTotal(procedimiento.getValor());
                }
            }
            Comisiones cmsn = ejbComision.traer(profesional, procedimiento);
            if (cmsn == null) {
                comision = ejbComision.guardar(comision);
                if (comision.getIdComision() != null) {
                    obj = new JSONObject();
                    obj.put("idComision", comision.getIdComision());
                } else {
                    obj = new JSONObject();
                    obj.put("error", "Error al guardar comision");
                }
            } else if (cmsn.getEstado().equals("activo")) {
                obj = new JSONObject();
                obj.put("error", "El procedimiento " + procedimiento.getProcedimiento() + " " + procedimiento.getCups() + " ya esta asociado a este profesional");
            } else if (cmsn.getEstado().equals("inactivo")) {
                comision.setFechaCreacion(cmsn.getFechaCreacion());
                comision = ejbComision.editar(comision);
                if (comision.getIdComision() != null) {
                    obj = new JSONObject();
                    obj.put("idComision", comision.getIdComision());
                } else {
                    obj = new JSONObject();
                    obj.put("error", "Error al guardar comision");
                }
            }
        }
        array = listarProcedimientos(r);
        return array;
    }

    public JSONArray editarComision(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Comisiones comision = ejbComision.traer(Integer.parseInt(r.getParameter("idComision")));
        if (comision != null) {
            comision.setTipoComision(r.getParameter("tipoComision"));
            comision.setValorComision(Double.parseDouble(r.getParameter("valorComision")));
            comision.setTotal(Double.parseDouble(r.getParameter("total")));
            comision.setFechaCreacion(fechaActual);
            comision.setUltimaEdicion(fechaActual);
            comision.setIdClinica(sesion.clinica(r.getSession()));
            Procedimientos procedimiento = ejbProcedimiento.traer(comision.getIdProcedimiento().getIdProcedimiento());
            if (comision.getTipoComision().equals("procentual")) {
                if (procedimiento.getValor() < ((comision.getValorComision() * 100) / procedimiento.getValor())) {
                    comision.setTotal(procedimiento.getValor());
                }
            } else {
                if (procedimiento.getValor() < comision.getValorComision()) {
                    comision.setTotal(procedimiento.getValor());
                }
            }
            comision = ejbComision.editar(comision);
            obj = new JSONObject();
            obj.put("idComision", comision.getIdComision());
            array = listarComisiones(r);
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al editar comision");
            array.add(obj);
        }
        return array;
    }

    public Integer eliminarComision(HttpServletRequest r) {
        Integer ok = ejbComision.eliminar(Integer.parseInt(r.getParameter("idComision")));
        return ok;
    }

    public JSONArray listarComisiones(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        List<Profesionales> profesionales = ejbProfesional.listar(sesion.clinica(r.getSession()));
        if (profesionales != null) {
            for (Profesionales profesional : profesionales) {
                if (profesional.getEstado().equals("activo") && profesional.getComisionesList().size()>0) {
                    obj = new JSONObject();
                    obj.put("idProfesional", profesional.getIdProfesional());
                    obj.put("nombre", profesional.getNombre());
                    obj.put("apellido", profesional.getApellido());
                    obj.put("tipoDeDocumento", profesional.getIdTipoDeDocumento().getTipoDeDocumento());
                    obj.put("numeroDocumento", profesional.getNumeroDeDocumento());
                    obj.put("cantidadProcedimientos", profesional.getComisionesList().size()-1);
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray listarProcedimientos(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Profesionales profesional = ejbProfesional.traer(Integer.parseInt(r.getParameter("idProfesional")));
        List<Comisiones> comisiones = ejbComision.listar(profesional);
        if (comisiones != null) {
            for (Comisiones procedimiento : comisiones) {
                if (procedimiento.getEstado().equals("activo")) {
                    obj = new JSONObject();
                    obj.put("idRelProcedimientoConvenio", procedimiento.getIdComision());
                    obj.put("idProcedimiento", procedimiento.getIdProcedimiento().getIdProcedimiento());
                    obj.put("procedimiento", procedimiento.getIdProcedimiento().getProcedimiento());
                    obj.put("categoriaProcedimiento", procedimiento.getIdProcedimiento().getIdCategoriaProcedimiento().getCategoriaProcedimiento());
                    obj.put("tipoComision", procedimiento.getTipoComision());
                    obj.put("valorComision", procedimiento.getValorComision());
                    obj.put("total", procedimiento.getTotal());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray traerComision(HttpServletRequest r) {
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
