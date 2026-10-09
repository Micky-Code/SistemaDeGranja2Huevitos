package com.granja.dos.huevitos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBMigrator {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://ep-withered-shape-axz1o59w-pooler.c-4.us-east-2.aws.neon.tech:5432/neondb?sslmode=require&channelBinding=require";
        String user = "neondb_owner";
        String pass = "npg_3T5zUPYOmdBr";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
             
            System.out.println("Conectando a NeonDB...");
            
            int delDetalles = stmt.executeUpdate("DELETE FROM avicola.detalle_produccion WHERE produccion_id IN (SELECT id FROM avicola.produccion_diaria WHERE lote_galpon_id IN (SELECT id_lote_galpon FROM avicola.lotes_galpones WHERE id_lote IN (SELECT id_lote FROM avicola.lotes_aves WHERE raza IS NULL)))");
            System.out.println("Detalles prod eliminados: " + delDetalles);

            int delProd = stmt.executeUpdate("DELETE FROM avicola.produccion_diaria WHERE lote_galpon_id IN (SELECT id_lote_galpon FROM avicola.lotes_galpones WHERE id_lote IN (SELECT id_lote FROM avicola.lotes_aves WHERE raza IS NULL))");
            System.out.println("Produccion diaria eliminada: " + delProd);

            int deletedAsignaciones = stmt.executeUpdate("DELETE FROM avicola.lotes_galpones WHERE id_lote IN (SELECT id_lote FROM avicola.lotes_aves WHERE raza IS NULL)");
            System.out.println("Asignaciones antiguas eliminadas: " + deletedAsignaciones);
            
            int deletedLotes = stmt.executeUpdate("DELETE FROM avicola.lotes_aves WHERE raza IS NULL");
            System.out.println("Lotes antiguos eliminados: " + deletedLotes);
            
            System.out.println("Limpieza completada exitosamente");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
