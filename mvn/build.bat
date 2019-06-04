cd target/build
mkdir results
cd data
mkdir temp
cd ..

"%JAVA_HOME%\bin\jar.exe" uf %1 tool.properties
del tool.properties