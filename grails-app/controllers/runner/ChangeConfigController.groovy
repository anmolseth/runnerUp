package runner

import grails.converters.JSON

import java.nio.charset.Charset
import java.nio.file.Files

/**
 * @author : Anmol Seth and Ajit Deswal on 5/05/2016
 *
 */
class ChangeConfigController {

    def index() { }

    static String filePath = "/home/anmol/Desktop/external-config.groovy"

    def changeKeyValue()
    {
        //String externalFileName = grailsApplication.config.grails.config.locations[0]
        //String file = externalFileName.split("file:")[1]

        File fileProp = new File(filePath)
        List<String> lines = fileProp.readLines()
        List<String> modified = new ArrayList<>()

        Map keyValueMap = request.JSON as Map
        Map resultMap = ["modifiedKeys":[], "insertedKeys":[]]

        def keyValue = null;
        for(String fileKeys : lines)
        {
            for(String key : keyValueMap.keySet()){
                if (fileKeys.startsWith("${key}=")){
                    keyValue = keyValueMap.get(key);
                    if(keyValue instanceof String)
                        fileKeys = "${key}=\"${keyValue}\"";
                    else
                        fileKeys = "${key}=${keyValue}";
                    //grailsApplication.config.put(key, keyValue)
                    resultMap.modifiedKeys.add(key);
                    keyValueMap.remove(key);
                    break;
                }
            }
            modified.add(fileKeys)
        }

        keyValueMap.keySet().each { key->
            keyValue = keyValueMap.get(key);
            if(keyValue instanceof String)
                modified.add("${key}=\"${keyValue}\"")
            else
                modified.add("${key}=${keyValue}")
            //updateConfigIfExisting(key, keyValue);
            resultMap.insertedKeys.add(key);
        }
        Files.write(fileProp.toPath(), modified, Charset.defaultCharset())
        mergeLatesFile();
        render (["result" : "success", "operationDone": resultMap] as JSON)
    }

    boolean mergeLatesFile()
    {
        ConfigObject sluper = new ConfigSlurper().parse(new File(filePath).text)
        grailsApplication.config.merge(sluper)
        return  true;
    }


    def geValue(String key)
    {
        def value = null;
        if(grailsApplication.config.containsKey(key)){
            value = grailsApplication.config.get(key);
        }else {
            key.split("\\.").each {
                if (value == null)
                    value = grailsApplication.config."${it}"
                else
                    value = value."${it}"
            }
        }
        render(["result": value] as JSON)
    }

    def manualChange()
    {
        ConfigObject sluper = new ConfigSlurper().parse(new File(filePath).text)
        grailsApplication.config.merge(sluper)
        render (["result" : "success"] as JSON)
    }
}
