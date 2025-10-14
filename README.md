# API_SYPAGO

_Inicio de proyecto para los TestSYPAGO visión general de los casos de prueba detallados para la funcionalidades "002,003". Los casos de prueba, organizados de forma jerárquica y con una nomenclatura clara (FMT-XX), cubren una amplia gama de escenarios, incluyendo la validación de campos, la verificación de tipos de datos, la gestión de valores nulos o vacíos, y la comprobación de límites y caracteres especiales. El objetivo principal de estas pruebas es asegurar la robustez, fiabilidad y correcta manipulación de los datos en la implementación del proceso de inicio de débito, garantizando así el correcto funcionamiento del sistema bajo diversas condiciones de entrada._

## Comenzando 🚀
_En la estructura del proyecto podra precisar los paquetes donde se encuentran los scripts de integracion a soapUI._

Mira **Deployment** para conocer como desplegar el proyecto.


### Pre-requisitos 📋

_Al menos JDK 17_

```
https://www.java.com/es/
```

_SoapUI 5.8.0 En adelante_

```
https://www.soapui.org/downloads/soapui/
```


### Cargar Projecto SoapUI
_Dentro del proyecto ubique el paquete 'Data' extraiga el fichero (TestSYPAGO-soapui-project.xml) Y extraigalo_

_1. Abra el soapUI ubiquese en la funcion (import)_

_2. Seleccione el fichero (TestSYPAGO-soapui-project.xml)_

_3. En cada TestSuite generado se encontrara un Script Groovy llamado "Datos IBPs" este contendra la logica para instanciar la clase y llamar al Script en la ruta posteriormente especificada_

NOTA IMPORTANTE: Dentro de este Script se implemento el metodo Random a fin de seleccionar filas de forma aletoria con el fin de ir rotando los datos para cada caso de prueba, 
Tambien se ajusto para que dichos datos furan cargados en un contexto global.

```
//Contextualizar Lista de variables globales
context.ListIBPs = ReceptorIBPs;
```
```
//Contextualizar Lista random
context.ListRandom = randomIBPs;
log.info(context.ListRandom);
```

_4. Compile el .JAR desde el editor de su preferencia y ubiquelo en la siguiente direccion_
Nota: si el SoapUI esta abierto debera cerrarlo para que el mismo pueda reconocer el .JAR

```
C:\Program Files\SmartBear\SoapUI-5.9.0\bin\ext
```


### Implementacion 🔧

_Es necesario que antes de empezar se asegure de conectarse al ambiente correcto, para ello puede hacer uso de la VPN y credenciales suministrada por el proveedor y/o cliente_

_Una vez conectado al ambiente puede desplegar la taza de de los containers para llevar un seguimiento de las transacciones_

### Container Gateway

```
sudo docker logs -f sypago_gateway_ms
```

### Container REST API

```
sudo docker logs -f sypago_gateway_rest_api
```

## Implementacion importar (Import.xlsx)  Llamar desde SoapUI ⚙️

_1. Dentro del proyecto ubique el paquete 'Data' extraiga el fichero (Import.xlsx) y coloquelo en la unidad principal Ejemplo de ruta: "C:\"_


### Estructura del los Casos de Prueba 🔩

_Hasta el momento solo se validara los sub-productos (002, 003)_
```
"SubProduct": "002"
```
```
"SubProduct": "003"
```

### Descriptcion TestCase realizados GV1 ⌨️

```
https://docs.google.com/spreadsheets/d/1bPkuB1btwiFyQ4Kx1ihZ0arBCkBHAgzHdLjw5upm_jc/edit?gid=0#gid=0
```

### Descriptcion TestCase realizados GV2 ⌨️

```
https://docs.google.com/spreadsheets/d/1sBUMJrW_RO3AlfyXLleeZ27f8ICrYOj2eAqJGaT3jNQ/edit?gid=0#gid=0
```

## Descripcion TestCase realizados API

```
https://docs.google.com/spreadsheets/d/1pyaNX8Yr4uQi5y_efWYrSjYubMrabGra71UngboVr6g/edit?gid=0#gid=0
```

