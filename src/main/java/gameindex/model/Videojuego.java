package gameindex.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class Videojuego {

    // === Longitudes máximas de cada campo (en caracteres) =================
    public static final int LEN_TITULO        = 100;
    public static final int LEN_DESARROLLADOR =  80;
    public static final int LEN_PLATAFORMAS   = 120;
    public static final int LEN_GENERO        =  50;
    public static final int LEN_SINOPSIS      = 300;

 
    public static final int RECORD_SIZE =
            LEN_TITULO        * 2   // 200
            + LEN_DESARROLLADOR * 2 // 160
            + 4                     //   4  (año)
            + LEN_PLATAFORMAS   * 2 // 240
            + LEN_GENERO        * 2 // 100
            + LEN_SINOPSIS      * 2 // 600
            + 1;                    //   1  (eliminado)
    // = 1305 bytes

    private String  titulo;
    private String  desarrollador;
    private int año;
    private String  plataformas;
    private String  genero;
    private String  sinopsis;
    /** 0 = activo, 1 = eliminado lógicamente */
    private byte    eliminado;

   

    /** Constructor completo. */
    public Videojuego(String titulo, String desarrollador, int año,
                      String plataformas, String genero, String sinopsis) {
        this.titulo        = titulo;
        this.desarrollador = desarrollador;
        this.año = año;
        this.plataformas   = plataformas;
        this.genero        = genero;
        this.sinopsis      = sinopsis;
        this.eliminado     = 0;
    }

    /** Constructor vacío requerido para reconstruir desde disco. */
    public Videojuego() {
        this.titulo        = "";
        this.desarrollador = "";
        this.año = 0;
        this.plataformas   = "";
        this.genero        = "";
        this.sinopsis      = "";
        this.eliminado     = 0;
    }

  
    public byte[] toBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(RECORD_SIZE);
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            escribirStringFijo(dos, titulo,        LEN_TITULO);
            escribirStringFijo(dos, desarrollador, LEN_DESARROLLADOR);
            dos.writeInt(año);
            escribirStringFijo(dos, plataformas,   LEN_PLATAFORMAS);
            escribirStringFijo(dos, genero,         LEN_GENERO);
            escribirStringFijo(dos, sinopsis,       LEN_SINOPSIS);
            dos.writeByte(eliminado);
        } catch (IOException e) {
            throw new RuntimeException("Error serializando Videojuego", e);
        }
        return baos.toByteArray();
    }


    public static Videojuego fromBytes(byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);
        Videojuego v = new Videojuego();
        try {
            v.titulo        = leerStringFijo(dis, LEN_TITULO).trim();
            v.desarrollador = leerStringFijo(dis, LEN_DESARROLLADOR).trim();
            v.año = dis.readInt();
            v.plataformas   = leerStringFijo(dis, LEN_PLATAFORMAS).trim();
            v.genero        = leerStringFijo(dis, LEN_GENERO).trim();
            v.sinopsis      = leerStringFijo(dis, LEN_SINOPSIS).trim();
            v.eliminado     = dis.readByte();
        } catch (IOException e) {
            throw new RuntimeException("Error deserializando Videojuego", e);
        }
        return v;
    }

    private static void escribirStringFijo(DataOutputStream dos, String valor, int longitud)
            throws IOException {
        String s = (valor == null) ? "" : valor;
        // Truncar si excede
        if (s.length() > longitud) {
            s = s.substring(0, longitud);
        }
        // Escribir caracteres del string
        for (int i = 0; i < s.length(); i++) {
            dos.writeChar(s.charAt(i));
        }
        // Rellenar con espacios
        for (int i = s.length(); i < longitud; i++) {
            dos.writeChar(' ');
        }
    }

   
    private static String leerStringFijo(DataInputStream dis, int longitud)
            throws IOException {
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(dis.readChar());
        }
        return sb.toString();
    }


    public String getTitulo()                    { return titulo; }
    public void   setTitulo(String titulo)       { this.titulo = titulo; }

    public String getDesarrollador()                         { return desarrollador; }
    public void   setDesarrollador(String desarrollador)     { this.desarrollador = desarrollador; }

    public int getAño()          { return año; }
    public void setAño(int año)  { this.año = año; }

    public String getPlataformas()                       { return plataformas; }
    public void   setPlataformas(String plataformas)     { this.plataformas = plataformas; }

    public String getGenero()                  { return genero; }
    public void   setGenero(String genero)     { this.genero = genero; }

    public String getSinopsis()                    { return sinopsis; }
    public void   setSinopsis(String sinopsis)     { this.sinopsis = sinopsis; }

    public byte getEliminado()                     { return eliminado; }
    public void setEliminado(byte eliminado)       { this.eliminado = eliminado; }

    /** @return true si el registro está marcado como eliminado lógicamente */
    public boolean estaEliminado()                 { return eliminado == 1; }

    /** Marca el registro como eliminado lógicamente. */
    public void marcarEliminado()                  { this.eliminado = 1; }

    /** Marca el registro como activo. */
    public void marcarActivo()                     { this.eliminado = 0; }


    @Override
    public String toString() {
        return "Videojuego{" +
                "titulo='"        + titulo        + '\'' +
                ", desarrollador='" + desarrollador + '\'' +
                ", anio="         + año +
                ", plataformas='" + plataformas   + '\'' +
                ", genero='"      + genero         + '\'' +
                ", sinopsis='"    + sinopsis       + '\'' +
                ", eliminado="    + eliminado      +
                '}';
    }
}
