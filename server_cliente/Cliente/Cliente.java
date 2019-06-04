package Cliente;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.SocketException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

class VenCliente {
	private JFrame cliente;
	private JTextField texto;
	private JPasswordField texto2;
	private archivos conectado;
	private static JButton b1;
	private static JLabel l1;
	private static JLabel l2;
    private static String nombreMaquina = "localhost"; // usa el nombre de máquina por defecto
    private static String numPuerto = "13"; // número de puerto por defecto
	private static int puertoServidor;
  	private MiSocketStream miSocket;
  
  public VenCliente()
  {
	  //INTERFAZ GRAFICA
	  cliente = new JFrame( "Cliente" );
	  texto=new JTextField();
	  texto2=new JPasswordField();
	  b1=new JButton("Conecta");
	  l1=new JLabel("ID USUARIO");
	  l2=new JLabel("CONTRASEÑA");
	  cliente.setLayout(null);
	  cliente.setSize(400,200);
	  cliente.setResizable(false);
	  cliente.setVisible(true);
	  cliente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  texto.setBounds(200,40,100,20);
	  texto2.setBounds(200,70,100,20);
	  b1.setBounds(175,120,100,20);
	  l1.setBounds(100,40,100,20);
	  l2.setBounds(100,70,100,20);
	  cliente.add(texto);
	  cliente.add(texto2);
	  cliente.add(b1);
	  cliente.add(l1);
	  cliente.add(l2);
	  cliente.setLocationRelativeTo(null);
	  //FIN INTERFAZ GRAFICA
	  
      ActionListener accion = new ActionListener(){//Accion que se ejecutar al clicker buton identificar

          public void actionPerformed(ActionEvent e) {
              
              try {
				conectar();
			} catch (IOException e1) {
			}
          }
          
      };//fin accion boton
	  b1.addActionListener(accion); //añadir la accion al buton identificar 
	  
	  texto.addKeyListener(new KeyListener(){//METODO PARA LIMITAR ENTRADA TEXTO USUARIO
		  public void keyTyped(KeyEvent t)
		   {
			  if (texto.getText().length()==9)
			  	t.consume();
		  }

		@Override
		public void keyPressed(KeyEvent arg0) {}
		@Override
		public void keyReleased(KeyEvent e) {}});//FIN TEXTO USUARIO
	  
	  texto2.addKeyListener(new KeyListener(){//METODO PARA LIMITAR ENTRADA TEXTO PASS
		  public void keyTyped(KeyEvent t)
		   {
			  if (texto2.getPassword().length== 9)
			  	t.consume();
		  }

		@Override
		public void keyPressed(KeyEvent arg0) {}
		@Override
		public void keyReleased(KeyEvent e) {}});}  //FIN TEXTO PASS
  
  public void conectar() throws SocketException, IOException {//metodo para conectarnos
    	  puertoServidor = Integer.parseInt(numPuerto);
      	  miSocket = new MiSocketStream(nombreMaquina, puertoServidor);
      	  String n=texto.getText();//guardamos usuario para otros usos
      	  String p = new String(texto2.getPassword());//guardamos contraseña
      	  miSocket.enviaMensaje(n);
      	  miSocket.enviaMensaje(p);
      	  String resul=miSocket.recibeMensaje();
        if(resul.contains("no"))//si credenciales incorrectas informamos y  cerramos
      	  {
        	cliente.setVisible(false);
        	 miSocket.close();
        	 JOptionPane.showMessageDialog(null, "CREDENCIALES INCORRECTAS,SE CERRARA EL ACCESO", "ERROR DE IDENTIFICACIÓN", JOptionPane.ERROR_MESSAGE);
      	  System.exit(0);
      	  }else {//si correctas informamos y damos acceso
      		JOptionPane.showMessageDialog(null, "CREDENCIALES CORRECTAS", "IDENTIFICACIÓN CORRECTA", JOptionPane.INFORMATION_MESSAGE);
      		cliente.setVisible(false);	
      		conectado=new archivos(miSocket,nombreMaquina,puertoServidor,n,p);//objeto que encapsula todas las operaciones con el servidor
      		//cliente abrir conexion nueva para multitarea
      		//conectado=new archivos(miSocket);
      	  }
      }
}//fin class ventana cliente


class archivos{
	private static JFrame interfaz;
	private JTextField texto;
	private MiSocketStream miSocket;
	private static JButton b1;
	private static JButton b2;
	private static JLabel et1;
	private String con;
	private String usu;
	private JFileChooser select;
	archivos(MiSocketStream socket,String dir,int port,String user,String pass)
	{
		//INTERFAZ GRAFICA
		  miSocket=socket;
		  usu=user;
		  con=pass;
		  interfaz=new JFrame ("Interfaz Cliente");
		  interfaz.setResizable(false);
		  interfaz.setVisible(true);
		  interfaz.setLayout(null);
		  interfaz.setSize(400,200);
		  b1=new JButton("Descargar Archivo");
		  b2=new JButton("Subir Archivo");
		  et1 = new JLabel("Archivo a buscar :");
		  texto=new JTextField();
		  texto.setBounds(200,40,150,20);
		  interfaz.add(b1);
		  interfaz.add(b2);
		  interfaz.add(texto);
		  interfaz.add(et1);
		  b1.setBounds(50,120,150,20);
		  b2.setBounds(200,120,150,20);
		  et1.setBounds(20,40,150,20);
		  
		  //FIN INTERFAZ GRAFICA
		  interfaz.addWindowListener(new WindowAdapter() {//MODIFICAMOS METODO CUANDO SE CIERRA VENTANA PARA CERRAR SOCKET Y PROGRAMA			  
			  @Override
			   
			  public void windowClosing(WindowEvent e) {//metodo que al cerrar la ventana se cierre conexion y programa
				  try {
					miSocket.close();
					System.out.println("SOCKET CERRADO");
					System.exit(0);
				} catch (IOException e1) {} 
			  }});
		  
		  ActionListener accion = new ActionListener(){//Accion que se ejecutar al clicker buton subir

	          public void actionPerformed(ActionEvent e) {
	        	  File subir=elegirfichero();//elegimos fichero
	        	  if(subir==null)//si es nulo o no elegidos volvemos para evitar error
	        		  return;
	        	  try {
	        		miSocket.enviaMensaje("s");//enviamos operacion de subir
					subirfil(subir,subir.getName());//metodo subir cierra socket
					miSocket=new MiSocketStream(dir,port);//reinstanciamos el socket para mas usos
					miSocket.enviaMensaje(usu);
					miSocket.enviaMensaje(con);
					miSocket.recibeMensaje();//mensaje de id
					//Nuevo Socket asociado al mismo cliente para que pueda realizar mas de una op por conexion
				} catch (IOException e1) {}
	          }
	          
	      };//fin accion subida
	      
	      ActionListener accion2 = new ActionListener(){//Accion que se ejecutar al clicker buton descargar

	          public void actionPerformed(ActionEvent e) {
	        	  try {
	        		miSocket.enviaMensaje("d");
					descargarfil();//metodo descargar cierra el socket
					miSocket=new MiSocketStream(dir,port);//reinstaciamos el socket para mas usos
					miSocket.enviaMensaje(usu);
					miSocket.enviaMensaje(con);
					miSocket.recibeMensaje();//mensaje de id
					//Nuevo Socket asociado al mismo cliente para que pueda realizar mas de una op por conexion
				} catch (IOException e1) {}
	          }
	          
	      };//fin accion venta
	      
	      b2.addActionListener(accion);
	      b1.addActionListener(accion2);
	      interfaz.setLocationRelativeTo(null);
	}
private void subirfil(File subir,String name) throws IOException
{
	byte buffer[]=new byte[1024];//buffer
	miSocket.enviaMensaje(name);
	BufferedInputStream iF = new BufferedInputStream(new FileInputStream(subir)); 
	BufferedOutputStream oF=new BufferedOutputStream(miSocket.st().getOutputStream());
	int aux=0;
	while((aux = iF.read(buffer))!=-1)
	{
		oF.write(buffer,0,aux);	
	}

	JOptionPane.showMessageDialog(null, "COPIA ALMACENADA","FIN", JOptionPane.INFORMATION_MESSAGE);//avisamos de que copia termino
	iF.close();
}

private void descargarfil() throws IOException
{
	   miSocket.enviaMensaje(texto.getText());
	   String aa= miSocket.recibeMensaje();
	   if(texto.getText().isEmpty()){
		   JOptionPane.showMessageDialog(null,"PARACE QUE NO HAS ESCRITO NINGUN NOMBRE DE ARCHIVO","ERROR", JOptionPane.ERROR_MESSAGE);
		   return;}
	   File nuevo;
	   if(aa.contains("s"))//COMPROBAMOS QUE EL SERVIDOR AVISA QUE EXISTE ESE ARCHIVO EN CARPETA, SI EXISTE DESCARGA
	   {
		   if(System.getProperty("os.name").startsWith("Windows"))
			   nuevo=new File(System.getProperty("user.dir")+"\\"+texto.getText());//windows
	           else
	        	nuevo=new File(System.getProperty("user.dir")+"/"+texto.getText());//linux
	   nuevo.createNewFile();
	   BufferedInputStream datosllegan = new BufferedInputStream(miSocket.st().getInputStream());//buffer entrada socket
	   BufferedOutputStream escritura = new BufferedOutputStream(new FileOutputStream (nuevo));//buffer archivo
	   byte [] buffer= new byte[1024];
	   int fin=0;
	   while((fin=(datosllegan.read(buffer)))!=-1)
	   {
		   escritura.write(buffer,0,fin);//creamos archivo
	   }
	   JOptionPane.showMessageDialog(null,"FIN DESCARGA","FIN", JOptionPane.INFORMATION_MESSAGE);
	   escritura.close();
	   }else {//SINO EXISTE AVISA
		   JOptionPane.showMessageDialog(null,"NO EXISTE ESE FICHERO EN SU CARPETA","ERROR", JOptionPane.ERROR_MESSAGE);
	  }
}
	
	
private File elegirfichero()//METODO QUE DEVUELVE EL FICHERO ELEGIDO PARA SUBIR
{
	select=new JFileChooser();
	select.showOpenDialog(interfaz);
	File archivos=select.getSelectedFile();
	return archivos;
}
	
}
  
  public class Cliente{//CLASE QUE ENCAPSULA TODO LO ANTERIOR
	  static VenCliente ventana;
	  public static void main(String[ ] args) {
		  ventana = new VenCliente();
		     try {
		     }catch (Exception ex) {}
		   } // fin de main
  } // fin de class Cliente

