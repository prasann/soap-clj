(ns soap-clj.core
  (require [clj-http.client :as client])
  (import (javax.xml.soap MessageFactory)
          (javax.xml.bind JAXBContext)
          (de.geo GetGeoIP GetGeoIPResponse)
          (java.io ByteArrayOutputStream ByteArrayInputStream)))

(defn- soap-body-as-string [request-object]
  "Returns a soap message as string. This contains SoapEnvelope with SoapHeader and SoapBody.
  The request object is the root object inside the soap body"
  (let [message (-> (MessageFactory/newInstance)
                    (.createMessage))
        output-stream (ByteArrayOutputStream.)
        marshaller (-> (JAXBContext/newInstance (into-array [(class request-object)]))
                       (.createMarshaller))]
    (.marshal marshaller request-object (.getSOAPBody message))
    (doto message
      (.saveChanges)
      (.writeTo output-stream))
    (str output-stream)))

(defn- parse-response [klass-name soap-response]
  "Parses soap-response and returns an object of type klass-name.
  This should be the root element in the SoapBody"
  (let [unmarshaller (-> (JAXBContext/newInstance (into-array [klass-name]))
                         (.createUnmarshaller))
        soap-response-stream (-> (.getBytes soap-response "UTF-8")
                                 (ByteArrayInputStream.))
        node (->> (-> (MessageFactory/newInstance)
                      (.createMessage nil soap-response-stream))
                  (.getSOAPBody)
                  (.getFirstChild))]
    (.unmarshal unmarshaller node)))

(defn- post->soap-endpoint [ws-url soap-body]
  (let [response
        (client/post ws-url
                     {:body         soap-body
                      :content-type "text/xml"})]
    (:body response)))

(defn- geo-ip-request [ip]
  (let [geoIp (GetGeoIP.)]
    (.setIPAddress geoIp ip)
    geoIp))

(defn find-my-country [ip]
  (let [soap-request-body (soap-body-as-string (geo-ip-request ip))
        soap-response (post->soap-endpoint "http://www.webservicex.net/geoipservice.asmx" soap-request-body)
        value-obj (parse-response GetGeoIPResponse soap-response)]
    (.getCountryName (.getGetGeoIPResult value-obj))))