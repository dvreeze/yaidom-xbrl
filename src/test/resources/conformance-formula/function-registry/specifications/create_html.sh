targets="registry function conformance conformanceFunction"
directory="../../specifications/xbrlspec/"

touch ${directory}styles.xml
echo "<styles>" >> ${directory}styles.xml
cat ${directory}styles.css >> ${directory}styles.xml
echo "</styles>" >> ${directory}styles.xml

for target in $targets
do
	echo "----------------------------------------------------------"
	echo "Transforming ${target}.xml"
	java -jar ${directory}saxon8.jar -o ../pub/${target}_merged.xml ${target}.xml ${directory}merge.xsl
	echo "Transforming ${target}_merged.xml"
	java -jar ${directory}saxon8.jar -o ../pub/${target}.html ../pub/${target}_merged.xml ${directory}stylesheet.xsl
	rm ../pub/${target}_merged.xml
done

rm ${directory}styles.xml
