/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package saludtec.admincloud.web.servicios;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import saludtec.admincloud.web.utilidades.Archivo;
import saludtec.admincloud.web.utilidades.Calendario;
import saludtec.admincloud.web.utilidades.Sesion;

/**
 *
 * @author saintec
 */
@WebServlet(name = "ArchivosWeb", urlPatterns = {"/archivos/*"})
@MultipartConfig(location = "/var/www/AdminCloudData/temp")
public class ArchivosWeb extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    Sesion sesion = new Sesion();
    Date fechaActual = Calendario.fechaCompleta();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            String servicio = request.getPathInfo();
            String metodo = request.getMethod();
            String idPaciente = contenidoFormData(request.getPart("paciente").getInputStream());
            String contentType = request.getPart("file").getContentType();
            String ext = "";
            String ruta = "/var/www/AdminCloud/clinicas/" + sesion.clinica(request.getSession()).getIdClinica() + "/pacientes/" + idPaciente;
            switch (metodo) {
                case "POST":
                    switch (servicio) {
                        case "guardarPerfil":
                            ruta += "perfil/";
                            guardarFotoPerfil(request).writeJSONString(out);

                            break;

                        case "guardarGaleria":
                            ruta += "galeria/";
                            guardarArchivo(request, idPaciente, ext, ruta).writeJSONString(out);
                            break;

                        case "guardarGalerias":
                            ruta += "galeria/";
                            guardarArchivos(request, idPaciente, ext, ruta).writeJSONString(out);
                            break;

                        case "guardarAnexo":
                            ruta += "anexos/";
                            guardarArchivo(request, idPaciente, ext, ruta).writeJSONString(out);
                            break;

                        case "guardarAnexos":
                            ruta += "anexos/";
                            guardarArchivos(request, idPaciente, ext, ruta).writeJSONString(out);
                            break;
                    }
                    break;

                case "GET":
                    response.sendRedirect("http://localhost/AdminCloudData" + request.getPathInfo());
                    break;

                default:
                    response.sendError(501, "Metodo " + metodo + " no soportado");
                    break;
            }

        }

    }

    private JSONArray guardarFotoPerfil(HttpServletRequest r) throws ServletException, IOException {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        String idPaciente = contenidoFormData(r.getPart("paciente").getInputStream());
        String contentType = r.getPart("archivo").getContentType();
        String extArch = extensionArchivo(contentType);
        File ruta = new File("/var/www/AdminCloud/clinicas/" + sesion.clinica(r.getSession()).getIdClinica() + "/pacientes/" + idPaciente);
        r.getPart("archivo").write(idPaciente + extArch);
        if (ruta.exists()) {
            File archTemp = new File("var/www/AdminCloudData/temp/" + idPaciente + extArch);
            if (archTemp.renameTo(new File(ruta + idPaciente + extArch))) {
                //aca va el guardado en la base de datos
                obj = new JSONObject();
                obj.put("ruta", archTemp);
                array.add(obj);
            }
        } else {
            obj = new JSONObject();
            obj.put("error", "no se pudo guardar la imagen");
            array.add(obj);
        }
        return array;
    }

    private JSONArray guardarArchivo(HttpServletRequest r, String idPaciente, String extension, String ruta) throws ServletException, IOException {
        JSONArray array = new JSONArray();
        JSONObject obj = null;

        Archivo arch = new Archivo(sesion.clinica(r.getSession()).getIdClinica().toString(), idPaciente);
        r.getPart("archivo").write(idPaciente + extension);
        if (arch.crearDirectorioPaciente()) {
            File archTemp = new File("var/www/AdminCloudData/temp/" + idPaciente + extension);
            //este rename debe modificarse para que tome el id de la base de datos
            if (archTemp.renameTo(new File(ruta + idPaciente + extension))) {
                //aca va el guardado en la base de datos
                obj = new JSONObject();
                obj.put("ruta", arch);
                array.add(obj);
            }
        } else {
            obj = new JSONObject();
            obj.put("error", "no se pudo guardar la imagen");
            array.add(obj);
        }

        return array;
    }

    private JSONArray guardarArchivos(HttpServletRequest r, String idPaciente, String extension, String ruta) throws ServletException, IOException {
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        Integer cantidad = Integer.parseInt(contenidoFormData(r.getPart("cantidad").getInputStream()));
        Archivo arch = new Archivo(sesion.clinica(r.getSession()).getIdClinica().toString(), idPaciente);
        for (int i = 0; i < cantidad; i++) {
            r.getPart("archivo" + i).write(idPaciente + extension);
            if (arch.crearDirectorioPaciente()) {
                File archTemp = new File("var/www/AdminCloudData/temp/" + idPaciente + extension);
                //este rename debe modificarse para que tome el id de la base de datos
                if (archTemp.renameTo(new File(ruta + idPaciente + extension))) {
                    //aca va el guardado en la base de datos
                    obj = new JSONObject();
                    obj.put("ruta", arch);
                    array.add(obj);
                }
            } else {
                obj = new JSONObject();
                obj.put("error", "no se pudo guardar la imagen");
                array.add(obj);
            }
        }

        return array;
    }

    private static String contenidoFormData(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    private String extensionArchivo(String contentType) throws IOException {
        String ext = "";
        switch (contentType) {
            case "image/jpeg":
                ext = ".jpg";
                break;

            case "image/png":
                ext = ".png";
                break;

            case "image/gif":
                ext = ".gif";
                break;

            case "application/pdf":
                ext = ".pdf";
                break;

        }
        return ext;
    }

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
