package org.example;

import java.beans.ConstructorProperties;
import java.io.File;
import java.util.ArrayList;
import java.sql.*;

public class Main {
    private static Connection connection;
    public static void main(String[] args) {
        try{
            //  PASO 1 CREACION|MODIFICACION DE DEPARTAMENTOS ANTES DE ACTUALIZAR VALIDAR QUE YA SE HAYA CREADO EL CATALOGO DE LOS DEPARTAMENTOS
            //              VALIDAR QUE NO SE DUPLIQUE POR ESPACIONS

            //  Departamento depto = new Departamento();
            //  ArrayList<Departamento> lista = depto.ObtenerDepartamentos();
            //  depto.ActualizarDeptoHoteles(lista);

//************************************************************************************************************

            // PASO 2 CREACION|MODIOFICACION DE AREASCECOS
            //  NOTA: ANTES SE DEBE RENOMBRAR LA TABLA "Areaceco" a "AreacecoBackUp" y crear nueva tabla "Areaceco"
            //              VALIDAR QUE NO SE DUPLIQUE POR ESPACIOS
            // PASO 2.1 Pasar los areascecos a la nueva tabla

            //Areaceco.ObtenerAreaCecoAnteriores();
            
//************************************************************************************************************
            // PASO 3 ACTUALIZACION DE ARTICULOS CON IDS DEPARTAMENTOS
            // NOTA: RECUERDA AGREGAR EL CAMPO idDepartamento a la Tabla Articulos

            //Articulos.ActualizarArticulos();

//************************************************************************************************************
            //  PASO 4 ACTUALIZAR ID DEPARTAMENTO DE LAS SOLICITUDES
            //  NOTA: RECUERDA AGREGAR EL CAMPO idDepartamento en la tabla de Solicitudes
            //Solicitudes.ActualizarDepartamento();


//************************************************************************************************************

            //  PASO 5 ACTUALIZAR ID AREA DE LAS SOLICITUDES
            //  NOTA: RECUERDA AGREGAR EL CAMPO idAreaCeco en la tabla de Solicitudes
            
            Solicitudes.ActualizarArea();

        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    
}

class Conexion{
    public static Connection Consulta()
    {
        Connection connection = null;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url ="jdbc:mysql://10.84.129.40:3306/Lavanderia?user=lavanderia_user&password=KY75Dnd2E3trE5aNdHXDwyPF6";
            File file = new File("client-cert.pfx");
            String path = file.getCanonicalPath().toString();
            System.setProperty("javax.net.ssl.keyStore", path);
            System.setProperty("javax.net.ssl.keyStorePassword", "");
            connection = DriverManager.getConnection(url);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        return connection;
    }
}

class Departamento {
    public int id;
    public String nombre;        

    public ArrayList<Departamento> ObtenerDepartamentos(){
        ArrayList<Departamento> lista = new ArrayList<Departamento>();
        Connection conn = Conexion.Consulta();

        if(conn != null)
        {
            try{
                String consulta = "SELECT * FROM Departamento";
                Statement stmt = conn.createStatement();
                ResultSet result = stmt.executeQuery(consulta);

                while(result.next())
                {
                    Departamento depto = new Departamento();
                    depto.id = result.getInt("idDepartamento");
                    depto.nombre = result.getString("NombreDepartamento");
                    lista.add(depto);
                }
                conn.close();
            }
            catch(Exception e)
            {
                System.err.println(e.getMessage());
            }
        }

        return lista;
    }
    
    public String ActualizarDeptoHoteles(ArrayList<Departamento> lista)
    {        
        String result = "";

        for(Departamento depto : lista)
        {
            String query  = "UPDATE DepartamentosHoteles SET idDepartamento = " + depto.id + " WHERE nombreDepartamento = '" + depto.nombre + "'";
            result = EjecutarQuery(query);
            query  = "UPDATE Solicitudes SET idDepartamento = " + depto.id + " WHERE tipoBlancos = '" + depto.nombre + "';";
            result = EjecutarQuery(query);
            query = "UPDATE Articulos SET idDepartamento = " + depto.id + " WHERE grupo = '" + depto.nombre + "';";
            result = EjecutarQuery(query);
        }
        return result;        
    }    

    public String EjecutarQuery(String query)
    {
        String ejecutar = "";
        Connection conn = Conexion.Consulta();

        if(conn != null)
        {
            try{
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.executeUpdate();                
                conn.close();
                System.out.println("query ok :" + query);
                
            }
            catch(Exception e)
            {
                System.err.println(e.getMessage());
                ejecutar = "Fallo query";
            }
        }

        return ejecutar;
    }
}

class Areaceco{
    public int idAreaCeco;
    public String nombreAreaCeco;
    public String codigoCeco;
    public String idsHoteles;
    public int idDepartamento;
    //  FUNCIONES PARA CREAR CECOS NUEVOS EN BASE A LOS VIEJOS REGISTROS

    public static String ObtenerAreaCecoAnteriores()
    {
        String result = "";
        Connection conn = Conexion.Consulta();
        ArrayList<Areaceco> list = new ArrayList<>();

        if(conn != null)
        {
            try{
                String consulta = "SELECT "+
                                        "codigoCeco, "+
                                        "nombreAreaCeco, "+
                                        "GROUP_CONCAT(DISTINCT  a.idHotel) AS idHoteles, "+
                                        "a.idDepartamento "+
                                    "FROM DepartamentosHoteles a "+
                                    "INNER JOIN AreacecoBackUp c ON a.idDepartamentoHotel = c.idDepartamento "+
                                    "GROUP BY nombreDepartamento,codigoCeco,nombreAreaCeco,areaoceco, idDepartamento ORDER BY nombreAreaCeco;";
                Statement stmt = conn.createStatement();
                ResultSet resultQuery = stmt.executeQuery(consulta);

                while(resultQuery.next())
                {
                    Areaceco area = new Areaceco();
                    area.nombreAreaCeco = resultQuery.getString("nombreAreaCeco");
                    area.codigoCeco = resultQuery.getString("codigoCeco");
                    area.idsHoteles = resultQuery.getString("idHoteles");
                    area.idDepartamento = resultQuery.getInt("idDepartamento");
                    list.add(area);
                }
                conn.close();
                Areaceco areaceco = new Areaceco();                
                areaceco.InsertarNuevaTabla(list);
            }
            catch(Exception e)
            {
                System.err.println(e.getMessage());
            }
        }        
        return result;
    }
    public String InsertarNuevaTabla(ArrayList<Areaceco> lista){
        String result = "";
        Connection conn = Conexion.Consulta();

        if(conn != null)
        {
            try{
                for(Areaceco item : lista)
                {
                    String queryInsert = "INSERT INTO Areaceco (nombreAreaCeco, codigoCeco) VALUES ('" + item.nombreAreaCeco + "',  '" + item.codigoCeco + "');";
                    PreparedStatement stmt = conn.prepareStatement(queryInsert);
                    stmt.executeUpdate();        
                    
                    String sqlGetId = "SELECT LAST_INSERT_ID();";
					PreparedStatement stmtGetId = conn.prepareStatement(sqlGetId);
					ResultSet resultSet = stmtGetId.executeQuery();

                    while (resultSet.next()) {
						item.idAreaCeco = resultSet.getInt(1);
					}

                    String[] idshoteles = item.idsHoteles.split(",");

                    for(int i = 0; i < idshoteles.length; i++)
                    {
                        String inserPivote = "INSERT INTO Area_Depto_Hotel (idAreaCeco, idDepartamento, idHotel) VALUES ("+item.idAreaCeco+", "+item.idDepartamento+", '"+idshoteles[i]+"');";
                        PreparedStatement prestmt = conn.prepareStatement(inserPivote);
                        prestmt.executeUpdate();
                    }

                    System.out.println("Se inserto: "+item.nombreAreaCeco);
                }
                conn.close();
                result = "Ok";
            }
            catch(Exception e)
            {
                System.err.println(e.getMessage());
                result = "Fallo";
            }
        }
        return result;
    }

    
    // // FUNCIONES PARA ACTUALIZAR LOS CECOS CON LOS NUEVOS IDS DE LOS DEPTOS

    // public static String ActualizarPivoteDepto()
    // {
    //     Connection conn = Conexion.Consulta();
    //     ArrayList<AreacecoPivote> list = new ArrayList<>();
    //     if(conn != null)
    //     {
    //         try{
    //             String consulta = "SELECT a.nombreAreaCeco, dh.idDepartamento "+
    //                             "FROM Lavanderia.AreacecoBackUp a "+
    //                             "LEFT JOIN DepartamentosHoteles dh ON a.idDepartamento = dh.idDepartamentoHotel "+
    //                             "WHERE dh.nombreDepartamento is not null group by nombreAreaCeco, idDepartamento;";  

    //             Statement stmt = conn.createStatement();
    //             ResultSet result = stmt.executeQuery(consulta);

    //             while(result.next())
    //             {
    //                 AreacecoPivote acpivote = new AreacecoPivote();
    //                 acpivote.nombreArea = result.getString("nombreAreaCeco");
    //                 acpivote.nuevoIdDepartamento = result.getInt("idDepartamento");
    //                 list.add(acpivote);
    //             }

    //             conn.close();
    //             Areaceco areaceco = new Areaceco();
    //             areaceco.EjecutarActualizarPivoteDepto(list);
    //         }
    //         catch(Exception e)
    //         {
    //             System.err.println(e.getMessage());
    //         }
    //     }

    //     return "";
    // }
    // public String EjecutarActualizarPivoteDepto(ArrayList<AreacecoPivote> list)
    // {
    //     Connection conn = Conexion.Consulta();

    //     if(conn != null)
    //     {
    //         try{
    //             for(AreacecoPivote item : list)
    //             {
    //                 String slqUpdate = "UPDATE Areaceco SET idDepartamento = " + item.nuevoIdDepartamento + " WHERE nombreAreaCeco = '" + item.nombreArea + "';";
    //                 PreparedStatement stmt = conn.prepareStatement(slqUpdate);
    //                 stmt.executeUpdate();
    //                 System.out.println("Se actualizo pivote con id departamento: "+item.nuevoIdDepartamento);
    //             }
    //             conn.close();
    //         }
    //         catch(Exception e)
    //         {
    //             System.err.println(e.getMessage());
    //         }
    //     }
    //     return "";
    // }


    // // FUNCIONES PARA ACTUALIZAR TABLA PIVOTES CECOS x DEPTO x HOTELES


    // public static String ConsultaPivoteCecos()
    // {
    //     String result =  "";
    //     Connection conn = Conexion.Consulta();
    //     ArrayList<AreaCecoPivoteDeptoHoteles> list = new ArrayList<>();

    //     if(conn != null)
    //     {
    //         try{
    //             String consulta = "SELECT " + 
    //             "ab.nombreAreaCeco, "+
    //             "GROUP_CONCAT(distinct dh.idHotel) as idHoteles, "+
    //             "dh.nombreDepartamento, "+
    //             "a.idAreaCeco, "+
    //             "d.idDepartamento "+
    //             "FROM DepartamentosHoteles dh "+
    //             "INNER JOIN AreacecoBackUp ab on dh.idDepartamentoHotel = ab.idDepartamento "+
    //             "INNER JOIN Areaceco a ON ab.nombreAreaCeco = a.nombreAreaCeco "+
    //             "INNER JOIN Departamento d ON dh.idDepartamento = d.idDepartamento "+
    //             "GROUP BY nombreDepartamento,nombreAreaCeco,areaoceco, idAreaCeco, idDepartamento;";
    //             Statement stmt = conn.createStatement();
    //             ResultSet resultQuery = stmt.executeQuery(consulta);

    //             while(resultQuery.next())
    //             {
    //                 AreaCecoPivoteDeptoHoteles area = new AreaCecoPivoteDeptoHoteles();
    //                 area.idHoteles = resultQuery.getString("idHoteles");
    //                 area.idAreaCeco = resultQuery.getInt("idAreaCeco");
    //                 area.idDepartamento = resultQuery.getInt("idDepartamento");
    //                 list.add(area);
    //             }

    //             conn.close();

    //             Areaceco area = new Areaceco();
    //             area.ActualizarPivoteAreaCecoHotel(list);
    //         }
    //         catch(Exception e)
    //         {
    //             System.err.println(e.getMessage());
    //         }
    //     }
    //     return result;
    // }
    // public String ActualizarPivoteAreaCecoHotel(ArrayList<AreaCecoPivoteDeptoHoteles> list)
    // {
    //     String result =  "";
    //     Connection conn = Conexion.Consulta();

    //     if(conn != null)
    //     {
    //         try{
    //             for(AreaCecoPivoteDeptoHoteles area : list)
    //             {
    //                 String[] idHoteles = area.idHoteles.split(",");
    //                 for(int i = 0; i < idHoteles.length; i++)
    //                 {
    //                     String queryInsert = "INSERT INTO Area_Depto_Hotel (idAreaCeco, idDepto, idHotel) VALUES ("+area.idAreaCeco+", "+area.idDepartamento+", '"+idHoteles[i]+"');";
    //                     PreparedStatement stmt = conn.prepareStatement(queryInsert);
    //                     stmt.executeUpdate();
    //                     System.out.println("Actualizado: "+area.idAreaCeco);
    //                 }

    //             }
    //             conn.close();
    //         }
    //         catch(Exception e)
    //         {
    //             System.err.println(e.getMessage());
    //         }
    //     }
    //     return result;
    // }
}

class Articulos{
    public int idArticulo;
    public int idDepartamento;

    public static String ActualizarArticulos()
    {
        
        Connection conn = Conexion.Consulta();
        if(conn != null)
        {
            try{
                String consulta = "SELECT a.idArticulo, a.descripcion, dh.idDepartamento FROM Articulos a "+
                                    "LEFT JOIN DepartamentosHoteles dh ON a.grupo = dh.nombreDepartamento "+
                                    "GROUP BY a.idArticulo, a.descripcion, dh.idDepartamento;";
                Statement stmt = conn.createStatement();
                ResultSet result = stmt.executeQuery(consulta);

                while(result.next())
                {
                    String sqlUpdate = "UPDATE Articulos SET idDepartamento = "+result.getInt("idDepartamento")+" WHERE idArticulo = "+result.getInt("idArticulo")+";";
                    PreparedStatement prstmt = conn.prepareStatement(sqlUpdate);
                    prstmt.executeUpdate();
                    System.out.println(result.getString("descripcion"));
                }
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }

        return "";
    }
}

class Solicitudes{
    public int idSolicitud;
    public int idDepartamento;
    public String hotel;
    public String tipoBlancos;

    public static String ActualizarDepartamento()
    {
        Connection conn = Conexion.Consulta();

        if(conn != null)
        {
            try{
                
                String consulta = "SELECT s.idSolicitud, dh.idDepartamento FROM Solicitudes s "+
                            "LEFT JOIN DepartamentosHoteles dh ON s.tipoBlancos = dh.nombreDepartamento "+
                            "WHERE s.hotel = dh.idHotel GROUP BY idSolicitud, idDepartamento;";
                Statement stmt = conn.createStatement();
                ResultSet result = stmt.executeQuery(consulta);

                while(result.next())
                {

                    String updatequery = "UPDATE Solicitudes SET idDepartamento = "+result.getInt("idDepartamento")+" WHERE idSolicitud = "+result.getInt("idSolicitud")+";";
                    PreparedStatement pstmt = conn.prepareStatement(updatequery);
                    pstmt.executeUpdate();
                    System.out.println("solicitud con id: "+result.getInt("idSolicitud")+" Actualizado");                    
                }                
            }
            catch(Exception e)
            {
                System.err.println(e.getMessage());
            }
        }

        return "";
    }
    public static String ActualizarArea()
    {
        Connection conn = Conexion.Consulta();

        if(conn != null)
        {
            try{
                String consulta = "SELECT s.idSolicitud, adh.idAreaCeco,  a.idAreaceco, a.nombreAreaCeco FROM Solicitudes s "+
                            "LEFT JOIN Area_Depto_Hotel adh ON s.idDepartamento = adh.idDepartamento "+
                            "LEFT JOIN Areaceco a ON a.idAreaceco = adh.idAreaCeco " +
                            "WHERE s.hotel = adh.idHotel AND s.area = a.nombreAreaCeco AND s.idAreaCeco = 0;";

                Statement stmt = conn.createStatement();
                ResultSet result = stmt.executeQuery(consulta);

                while(result.next())
                {
                    String queryUpdate = "UPDATE Solicitudes SET idAreaCeco ="+result.getInt("idAreaceco")+" WHERE idSolicitud = "+result.getInt("idSolicitud")+";";
                    PreparedStatement pstmt = conn.prepareStatement(queryUpdate);
                    pstmt.executeUpdate();
                    System.out.println("Solicitud con id: "+result.getInt("idSolicitud")+" Actualziado");
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getMessage());
            }
        }

        return "";
    }
}



//CLASES DE APOYO

class AreacecoPivote
{ 
    public String nombreArea;
    public int nuevoIdDepartamento;
}

class AreaCecoPivoteDeptoHoteles{
    public String idHoteles;
    public int idAreaCeco;
    public int idDepartamento;
}