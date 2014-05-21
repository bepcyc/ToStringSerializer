# Simple String serializer

This software provided AS IS. This means you can use it in any way you'd like.
As I'm (Viacheslav Rodionov) the author of this code I'd like to let it stay this way. Please don't delete @author tag in JavaDoc.

##About

The idea of this project is to provide a universal static toString method which will use only core java and will produce JSON (JSON-like for now) for any object.
Sometimes I need this serialization, because of already overriden toString() method, company prohibition on using other libraries or something else.
The serializer is useful for mocking runtime objects for unit-tests or just for pretty-printing.

##Usage

Just checkout or add __Strings.java__ file into your project.
Change LOCAL_PACKAGE_PREFIXES set in java file so it will contain only prefixes which are local for your project. Usually it's something like "com.mycompany."

Usage example:

    public static void main(String[] args) {
        List<String> strList = new ArrayList<String>() {
            {
                add("value1");
                add("value2");
            }
        };        
        System.out.println(Strings.toString(strList));
    }

This snippet will provide an output like this:

    {
      { "Collection" : [ 
       { "String" : "value1" }, 
       { "String" : "value2" }
      ] }
    }


##Dependencies

Java SE 1.7 or later

##TODO

* JSON-compliance
* formatting


