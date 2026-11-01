package modelo.personas;

public abstract class Persona {
    protected String nombre;
    protected String apellido;
    protected String documento;
    protected String telefono;
    protected String email;

    //Constructor

    public Persona(String nombre,String apellido,String documento,String telefono,String email){
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
        this.telefono=telefono;
        this.email = email;
    }

    //Getter y setters

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDocumento() {
        return documento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    //Metodo de nombre completo

    public String getNombreCompleto(){
        return nombre + " " + apellido;
    }

    
}