package co.matisses.carriers.coordinadora;

import co.matisses.carriers.coordinadora.ws.ArrayOfCotizadorDetalleempaques;
import co.matisses.carriers.coordinadora.ws.ArrayOfInt;
import co.matisses.carriers.coordinadora.ws.CotizadorCotizarIn;
import co.matisses.carriers.coordinadora.ws.CotizadorCotizarOut;
import co.matisses.carriers.coordinadora.ws.CotizadorDetalleEmpaques;
import co.matisses.carriers.coordinadora.ws.RpcServerSoapManagerPort;
import co.matisses.carriers.coordinadora.ws.RpcServerSoapManagerService;
import co.matisses.carriers.coordinadora.ws.SeguimientoSimpleIn;
import co.matisses.carriers.coordinadora.ws.SeguimientoSimpleOut;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dbotero
 */
public class CoordinadoraWSMain {

    private static final String API_KEY = "4b8a29dc-c92d-11e5-9956-625662870761";
    private static final String CLAVE = "36U1qY{H$656J";
    private static final String CUENTA = "1";
    private static final String DIV = "01";
    private static final String NIT = "900060329";

    /**
     *
     * @param wsdl ruta para el wsdl del servicio
     * @param ciudadDestino el codigo de 8 caracteres de la ciudad destino. Si
     * la ciudad es Medellin, por ejemplo (05001), codigo a enviar debe ser
     * 05001000
     * @param articulos mapa que contiene la ciudad de origen de los productos
     * que vienen en la lista. Esta lista contiene el detalle de cada producto
     * en el siguiente orden: 1.alto, 2. ancho, 3. largo, 4. peso, 5. unidades,
     * 6. valor
     * @return valor total del envio
     * @throws java.lang.Exception
     */
    public int cotizarEnvio(String wsdl, String ciudadDestino, Map<String, List<String[]>> articulos) throws Exception {
        RpcServerSoapManagerService service = new RpcServerSoapManagerService(new URL(wsdl));
        int valoracionMcia = 0;
        int totalEnvio = 0;
        RpcServerSoapManagerPort port = service.getRpcServerSoapManagerPort();
        for (String ciudadOrigen : articulos.keySet()) {
            valoracionMcia = 0;
            CotizadorCotizarIn request = new CotizadorCotizarIn();
            request.setApikey(API_KEY);
            request.setClave(CLAVE);
            request.setCuenta(CUENTA);
            request.setDiv(DIV);
            request.setNit(NIT);
            request.setProducto("0");
            request.setDestino(ciudadDestino);
            request.setOrigen(ciudadOrigen);

            ArrayOfInt nivelServ = new ArrayOfInt();
            nivelServ.getItem().add(0);
            request.setNivelServicio(nivelServ);

            ArrayOfCotizadorDetalleempaques detalle = new ArrayOfCotizadorDetalleempaques();
            List<String[]> items = articulos.get(ciudadOrigen);
            for (String[] item : items) {
                CotizadorDetalleEmpaques itm = new CotizadorDetalleEmpaques();
                itm.setAlto(item[0]);
                itm.setAncho(item[1]);
                itm.setLargo(item[2]);
                itm.setPeso(item[3]);
                itm.setUnidades(item[4]);
                itm.setUbl("0");
                detalle.getItem().add(itm);
                valoracionMcia += Integer.parseInt(item[5]) * Integer.parseInt(item[4]);
            }
            request.setValoracion(Integer.toString(valoracionMcia));
            request.setDetalle(detalle);
            CotizadorCotizarOut response = port.cotizadorCotizar(request);
            totalEnvio += response.getFleteTotal();
        }
        return totalEnvio;
    }

    public static String[] rastrearEnvio(String wsdl, String guia) throws Exception {
        String[] resultado = {null, null};
        try {
            RpcServerSoapManagerService service = new RpcServerSoapManagerService(new URL(wsdl));
            RpcServerSoapManagerPort port = service.getRpcServerSoapManagerPort();
            SeguimientoSimpleIn s = new SeguimientoSimpleIn();
            s.setAnexo(0);
            s.setApikey(API_KEY);
            s.setClave(CLAVE);
            s.setCodigoRemision(null);
            s.setDiv(DIV);
            s.setImagen(0);
            s.setNit(NIT);
            s.setReferencia(guia);
            SeguimientoSimpleOut response = port.seguimientoSimple(s);
            resultado[0] = response.getEstado().getDescripcion();
            resultado[1] = response.getEstado().getFecha();
        } catch (Exception e) {
        }
        return resultado;
    }

    public static void main(String[] args) {
        CoordinadoraWSMain coordinadora = new CoordinadoraWSMain();

        Map<String, List<String[]>> articulos = new HashMap<>();

        String[] art = new String[6];

        art = new String[]{"20", "3", "3", "3", "1", "74000"};

        if (articulos.containsKey("05631000")) {
            articulos.get("05631000").add(art);
        } else {
            List<String[]> sal = new ArrayList<>();
            sal.add(art);

            articulos.put("05631000", sal);
        }

        try {
            System.out.println(coordinadora.cotizarEnvio("https://ws.coordinadora.com/ags/1.4/server.php?wsdl", "05234000", articulos));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
