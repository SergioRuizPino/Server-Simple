package Servidor;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import Servidor.MiSocketStream;


public class Servidor implements Runnable{
	static File user = new File("C:\\servidorDatos.txt");//path fichero datos usuarios, si fichero cambia,cambiar path
	static HashMap<Integer,MiSocketStream>  miSocketDatos=new HashMap<Integer,MiSocketStream>();
	private Integer I;
	private String ruta;
	public Servidor(int i,MiSocketStream s)
	{
		I=i;
		miSocketDatos.put(I, s);
	}
	
   public static void main(String[] args) throws FileNotFoundException {
	 int i=0;
     int puertoServidor = 13; // puerto por defecto
     ExecutorService ejecutor = Executors.newFixedThreadPool(8);
     try
     {  
       ServerSocket  miSocketConexion =  new ServerSocket(puertoServidor);
       while (true) 
       {
         ejecutor.execute(new Servidor(i++,new MiSocketStream(miSocketConexion.accept())));//cada cliente, un hilo
       }
     } //try
     catch (Exception ex)
     {
       ex.printStackTrace( );
     }
   } //main
   
   private boolean  identificado(String usu , String pass,int i) throws IOException//metodo que identifica a los usuarios
   {
		FileReader lec = new FileReader (user);
		BufferedReader reader = new BufferedReader(lec);
        String aux;
        boolean sbucle=true;
        aux=reader.readLine();
        while(aux!=null && sbucle){
       	 
            if(usu.equals(aux))
            	sbucle=false;
       
          aux=reader.readLine();
        }
       if(aux==null)
        { miSocketDatos.get(I).enviaMensaje("no");
          miSocketDatos.get(I).close();
         reader.close();
         return false;
        }
       
  
        if(pass.equals(aux))
       {
       	 miSocketDatos.get(I).enviaMensaje("yes");
           reader.close();
           if(System.getProperty("os.name").startsWith("Windows"))
           	ruta = "\\" + usu+"\\";
           else
           	ruta = "/" + usu+"/";
   	    return true;
         
       }	 
        miSocketDatos.get(I).enviaMensaje("no");
        reader.close();
        return false;
	   
   }
   
   private void descargarFichero() throws IOException//metodo que descargar archivo al cliente
   {
	    String nomFichero = miSocketDatos.get(I).recibeMensaje();
		byte buffer[]=new byte[1024];
		File nuevo=new File(ruta+nomFichero);
		
		if(nuevo.exists())
		{//comprueba si fichero existe
		miSocketDatos.get(I).enviaMensaje("sss");//avisa si existe
		BufferedInputStream iF = new BufferedInputStream(new FileInputStream(nuevo)); 
		BufferedOutputStream oF=new BufferedOutputStream(miSocketDatos.get(I).st().getOutputStream());
		int aux=0;
		while((aux = iF.read(buffer))!=-1)
		{
			oF.write(buffer,0,aux);	
		}
		iF.close();
		oF.close();
		}else {//sino existe avisa
			miSocketDatos.get(I).enviaMensaje("nnn");
		}
	   
   }
   
   private void subirFichero() throws IOException//metodo que almacena archivo en servidor
   {
	   String nomFichero = miSocketDatos.get(I).recibeMensaje();
	   File nuevo=new File(ruta+nomFichero);
	   byte [] buffer= new byte[1024];
	   nuevo.createNewFile();
	   BufferedInputStream datosllegan = new BufferedInputStream(miSocketDatos.get(I).st().getInputStream());
	   BufferedOutputStream escritura = new BufferedOutputStream(new FileOutputStream (nuevo));//buffer archivo
	   int fin=0;
	   while((fin=(datosllegan.read(buffer)))!=-1)
	   {
		   escritura.write(buffer,0,fin);
	   }
		escritura.close();
		datosllegan.close();
   }

   
   public void run() //metodo run concurrente para que varios clientes puedan acceder a la vez 
   {
	   String operacion;
	   try
	   {
       String usu= miSocketDatos.get(I).recibeMensaje();
       String pass=miSocketDatos.get(I).recibeMensaje();
       if(!identificado(usu,pass,I))
       {
      	 miSocketDatos.get(I).close();
      	 miSocketDatos.remove(I);
       } 
       operacion = miSocketDatos.get(I).recibeMensaje();
       switch(operacion.charAt(0))
       {
       case 'd':descargarFichero();break;
       case 's':subirFichero();break;
       default:break;

       }
       miSocketDatos.get(I).close();
       miSocketDatos.remove(I);
   }catch(Exception e) {}
   }
}

