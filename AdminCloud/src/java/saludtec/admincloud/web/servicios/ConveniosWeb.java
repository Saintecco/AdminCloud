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
import saludtec.admincloud.ejb.entidades.CategoriasProcedimientos;
import saludtec.admincloud.ejb.entidades.Convenios;
import saludtec.admincloud.ejb.entidades.Procedimientos;
import saludtec.admincloud.ejb.entidades.RelProcedimientosConvenios;
import saludtec.admincloud.web.utilidades.Calendario;
import saludtec.admincloud.web.utilidades.Sesion;

/**
 *
 * @author saintec
 */
@WebServlet(name = "ConveniosWeb", urlPatterns = {"/convenios/*"})
public class ConveniosWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @EJB
    ConveniosEjb ejbConvenio;
    @EJB
    RelProcedimientosConveniosEjb ejbProcedimientoConvenio;
    @EJB
    ProcedimientosEjb ejbProcedimiento;
    @EJB
    RelProcedimientosConveniosEjb ejbRelProcedimientoConvenio;
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

                case "POST":
                    switch (servicio) {
                        case "/guardar":
                            guardarConvenio(request).writeJSONString(out);
                            break;

                        case "/guardarProcedimiento":
                            guardarProcedimiento(request).writeJSONString(out);
                            break;

                        case "/guardarProcedimientos":
                            guardarProcedimientos(request).writeJSONString(out);
                            break;

                        case "/editar":
                            editarConvenio(request).writeJSONString(out);
                            break;

                        case "/eliminar":
                            Integer rsp = eliminarConvenio(request);
                            if (rsp == 200) {
                                listarConvenios(request).writeJSONString(out);
                            } else {
                                response.sendError(400, "Convenio no eliminado");
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
                            listarConvenios(request).writeJSONString(out);
                            break;

                        case "/listarProcedimientos":
                            listarProcedimientos(request).writeJSONString(out);
                            break;

                        case "/traer":
                            traerConvenio(request).writeJSONString(out);
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

    // <editor-fold defaultstate="collapsed" desc="Metodos CRUD tipos de convenios">
    public JSONArray guardarConvenio(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Convenios convenio = new Convenios();
        convenio.setConvenio(r.getParameter("convenio"));
        convenio.setCodigoConvenio(r.getParameter("codigoConvenio"));
        convenio.setEstado("activo");
        convenio.setFechaCreacion(fechaActual);
        convenio.setUltimaEdicion(fechaActual);
        convenio.setIdClinica(sesion.clinica(r.getSession()));
        if (ejbConvenio.traer(convenio.getCodigoConvenio(), sesion.clinica(r.getSession())) == null) {
            convenio = ejbConvenio.guardar(convenio);
            if (convenio.getIdConvenio() != null) {
                obj = new JSONObject();
                obj.put("idConvenio", convenio.getIdConvenio());
                array = listarConvenios(r);
            } else {
                obj = new JSONObject();
                obj.put("error", "Error al guardar convenio.");
                array.add(obj);
            }
        } else {
            obj = new JSONObject();
            obj.put("error", "Ya existe un convenio con el codigo " + convenio.getCodigoConvenio());
            array.add(obj);
        }
        return array;
    }

    public JSONArray guardarProcedimiento(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Convenios convenio = ejbConvenio.traer(Integer.parseInt(r.getParameter("idConvenio")));
        Procedimientos procedimiento = ejbProcedimiento.traer(Integer.parseInt(r.getParameter("idProcedimiento")));
        RelProcedimientosConvenios relProcedimeintoConvenio = new RelProcedimientosConvenios();
        relProcedimeintoConvenio.setIdConvenio(convenio);
        relProcedimeintoConvenio.setIdProcedimiento(procedimiento);
        relProcedimeintoConvenio.setTipoDescuento(r.getParameter("tipoDescuento"));
        relProcedimeintoConvenio.setValorDescuento(Double.parseDouble(r.getParameter("valorDescuento")));
        relProcedimeintoConvenio.setTotal(Double.parseDouble(r.getParameter("total")));
        relProcedimeintoConvenio.setEstado("activo");
        relProcedimeintoConvenio.setFechaCreacion(fechaActual);
        relProcedimeintoConvenio.setUltimaEdicion(fechaActual);
        relProcedimeintoConvenio.setIdClinica(sesion.clinica(r.getSession()));
        if (relProcedimeintoConvenio.getTipoDescuento().equals("procentual")) {
            if (procedimiento.getValor() < ((relProcedimeintoConvenio.getValorDescuento() * 100) / procedimiento.getValor())) {
                relProcedimeintoConvenio.setTotal(0.0);
            }
        } else {
            if (procedimiento.getValor() < relProcedimeintoConvenio.getValorDescuento()) {
                relProcedimeintoConvenio.setTotal(0.0);
            }
        }
        RelProcedimientosConvenios rpc = ejbRelProcedimientoConvenio.traer(convenio, procedimiento);
        if (rpc == null) {
            relProcedimeintoConvenio = ejbRelProcedimientoConvenio.guardar(relProcedimeintoConvenio);
            if (convenio.getIdConvenio() != null) {
                obj = new JSONObject();
                obj.put("idRelProcedimientoConvenio", relProcedimeintoConvenio.getIdRelProcedimientoConvenio());
                array = listarProcedimientos(r);
            } else {
                obj = new JSONObject();
                obj.put("error", "Error al guardar procedimiento en el convenio.");
                array.add(obj);
            }
        } else if (rpc.getEstado().equals("activo")) {
            obj = new JSONObject();
            obj.put("error", "El procedimiento " + procedimiento.getProcedimiento() + " " + procedimiento.getCups() + " ya esta asociado a este convenio.");
            array.add(obj);
        } else if (rpc.getEstado().equals("inactivo")) {
            relProcedimeintoConvenio.setFechaCreacion(rpc.getFechaCreacion());
            relProcedimeintoConvenio = ejbRelProcedimientoConvenio.editar(relProcedimeintoConvenio);
            if (convenio.getIdConvenio() != null) {
                obj = new JSONObject();
                obj.put("idRelProcedimientoConvenio", relProcedimeintoConvenio.getIdRelProcedimientoConvenio());
                array = listarProcedimientos(r);
            } else {
                obj = new JSONObject();
                obj.put("error", "Error al guardar procedimiento en el convenio.");
                array.add(obj);
            }
        }
        return array;
    }

    public JSONArray guardarProcedimientos(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Convenios convenio = ejbConvenio.traer(Integer.parseInt(r.getParameter("idConvenio")));
        Integer idCategoriaProcedimiento = Integer.parseInt(r.getParameter("idCategoriaProcedimiento"));
        String tipoDescuento = r.getParameter("tipoDescuento");
        Double valorDescuento = Double.parseDouble(r.getParameter("valorDescuento"));
        Double total = Double.parseDouble(r.getParameter("total"));
        List<Procedimientos> procedimientos;
        if (idCategoriaProcedimiento == 0) {
            procedimientos = ejbProcedimiento.listar(sesion.clinica(r.getSession()));
        } else {
            procedimientos = ejbProcedimiento.listar(ejbCategoriaProcedimiento.traer(idCategoriaProcedimiento));
        }
        for (Procedimientos procedimiento : procedimientos) {
            RelProcedimientosConvenios relProcedimeintoConvenio = new RelProcedimientosConvenios();
            relProcedimeintoConvenio.setIdConvenio(convenio);
            relProcedimeintoConvenio.setIdProcedimiento(procedimiento);
            relProcedimeintoConvenio.setTipoDescuento(tipoDescuento);
            relProcedimeintoConvenio.setValorDescuento(valorDescuento);
            relProcedimeintoConvenio.setTotal(total);
            relProcedimeintoConvenio.setEstado("activo");
            relProcedimeintoConvenio.setFechaCreacion(fechaActual);
            relProcedimeintoConvenio.setUltimaEdicion(fechaActual);
            relProcedimeintoConvenio.setIdClinica(sesion.clinica(r.getSession()));
            if (relProcedimeintoConvenio.getTipoDescuento().equals("procentual")) {
                if (procedimiento.getValor() < ((relProcedimeintoConvenio.getValorDescuento() * 100) / procedimiento.getValor())) {
                    relProcedimeintoConvenio.setTotal(0.0);
                }
            } else {
                if (procedimiento.getValor() < relProcedimeintoConvenio.getValorDescuento()) {
                    relProcedimeintoConvenio.setTotal(0.0);
                }
            }
            RelProcedimientosConvenios rpc = ejbRelProcedimientoConvenio.traer(convenio, procedimiento);
            if (rpc == null) {
                relProcedimeintoConvenio = ejbRelProcedimientoConvenio.guardar(relProcedimeintoConvenio);
                if (convenio.getIdConvenio() != null) {
                    obj = new JSONObject();
                    obj.put("idRelProcedimientoConvenio", relProcedimeintoConvenio.getIdRelProcedimientoConvenio());
                } else {
                    obj = new JSONObject();
                    obj.put("error", "Error al guardar procedimiento en el convenio.");
                }
            } else if (rpc.getEstado().equals("activo")) {
                obj = new JSONObject();
                obj.put("error", "El procedimiento " + procedimiento.getProcedimiento() + " " + procedimiento.getCups() + " ya esta asociado a este convenio.");
            } else if (rpc.getEstado().equals("inactivo")) {
                relProcedimeintoConvenio.setFechaCreacion(rpc.getFechaCreacion());
                relProcedimeintoConvenio = ejbRelProcedimientoConvenio.editar(relProcedimeintoConvenio);
                if (convenio.getIdConvenio() != null) {
                    obj = new JSONObject();
                    obj.put("idRelProcedimientoConvenio", relProcedimeintoConvenio.getIdRelProcedimientoConvenio());
                } else {
                    obj = new JSONObject();
                    obj.put("error", "Error al guardar procedimiento en el convenio.");
                }
            }
        }
        array = listarProcedimientos(r);
        return array;
    }

    public JSONArray editarConvenio(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Convenios convenio = ejbConvenio.traer(Integer.parseInt(r.getParameter("idConvenio")));
        if (convenio != null) {
            convenio.setConvenio(r.getParameter("convenio"));
            convenio.setCodigoConvenio(r.getParameter("codigoConvenio"));
            convenio.setUltimaEdicion(fechaActual);
            convenio = ejbConvenio.editar(convenio);
            obj = new JSONObject();
            obj.put("idConvenio", convenio.getIdConvenio());
            array = listarConvenios(r);
        } else {
            obj = new JSONObject();
            obj.put("error", "Error al editar convenio.");
            array.add(obj);
        }
        return array;
    }

    public Integer eliminarConvenio(HttpServletRequest r) {
        Integer ok = ejbConvenio.eliminar(Integer.parseInt(r.getParameter("idConvenio")));
        return ok;
    }

    public JSONArray listarConvenios(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        List<Convenios> tiposDocumentos = ejbConvenio.listar(sesion.clinica(r.getSession()));
        if (tiposDocumentos != null) {
            for (Convenios convenio : tiposDocumentos) {
                if (convenio.getEstado().equals("activo")) {
                    obj = new JSONObject();
                    obj.put("idConvenio", convenio.getIdConvenio());
                    obj.put("convenio", convenio.getConvenio());
                    obj.put("codigoConvenio", convenio.getCodigoConvenio());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray listarProcedimientos(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Convenios convenio = ejbConvenio.traer(Integer.parseInt(r.getParameter("idConvenio")));
        List<RelProcedimientosConvenios> relProcedimientosConvenios = ejbProcedimientoConvenio.listar(convenio);
        if (relProcedimientosConvenios != null) {
            for (RelProcedimientosConvenios procedimiento : relProcedimientosConvenios) {
                if (procedimiento.getEstado().equals("activo")) {
                    obj = new JSONObject();
                    obj.put("idRelProcedimientoConvenio", procedimiento.getIdRelProcedimientoConvenio());
                    obj.put("idProcedimiento", procedimiento.getIdProcedimiento().getIdProcedimiento());
                    obj.put("procedimiento", procedimiento.getIdProcedimiento().getProcedimiento());
                    obj.put("categoriaProcedimiento", procedimiento.getIdProcedimiento().getIdCategoriaProcedimiento().getCategoriaProcedimiento());
                    obj.put("tipoDescuento", procedimiento.getTipoDescuento());
                    obj.put("valorDescuento", procedimiento.getValorDescuento());
                    obj.put("total", procedimiento.getTotal());
                    array.add(obj);
                }
            }
        }
        return array;
    }

    public JSONArray traerConvenio(HttpServletRequest r) {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Convenios convenio = ejbConvenio.traer(Integer.parseInt(r.getParameter("idConvenio")));
        if (convenio != null) {
            obj = new JSONObject();
            obj.put("idConvenio", convenio.getIdConvenio());
            obj.put("convenio", convenio.getConvenio());
            obj.put("codigoConvenio", convenio.getCodigoConvenio());
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
