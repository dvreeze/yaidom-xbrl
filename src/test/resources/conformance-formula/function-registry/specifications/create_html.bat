for %%f in (registry function conformance conformanceFunction) do (
java -jar ..\..\specifications\xbrlspec\libs\saxon9.jar -o ..\nopub\%%f_merged.xml %%f.xml ..\..\specifications\xbrlspec\merge.xsl
java -jar ..\..\specifications\xbrlspec\libs\saxon9.jar -o ..\nopub\%%f.html ..\nopub\%%f_merged.xml ..\..\specifications\xbrlspec\stylesheet.xsl  generate-css=0
del ..\nopub\%%f_merged.xml
)
pause
