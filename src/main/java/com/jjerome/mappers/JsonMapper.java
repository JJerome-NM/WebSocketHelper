package com.jjerome.mappers;

import lombok.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

public class JsonMapper {

    public static void main(String[] args) {

        String json = """
                {
                    "id": 1,
                    "cypher": "ІПЗ",
                    "academicYear": 1,
                    "number": 1.2,
                    "members": [
                        {"firstName": "Arsen", "lastName": "Sydoryk"},
                        {"firstName": "Arsen", "lastName": "Sydoryk"},
                        {"firstName": "Arsen", "lastName": "Sydoryk"}
                    ],
                    "map": [
                        {
                            "first": "second"
                        },
                        {
                            "second": "third"
                        }
                    ]
                }
                """;


        Group group = map(Group.class, json);
        System.out.println(group);


    }

    public static <T> T map(Class<T> clazz, String json){
        return map(clazz, new JSONObject(json));
    }

    @SneakyThrows
    public static <T> T map(Class<T> clazz, JSONObject jsonObject){
        Field[] fields = clazz.getDeclaredFields();
        String fieldName;
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        T obj = constructor.newInstance();
        for (Field field : fields) {

            fieldName = field.getName();
            if(!jsonObject.isNull(fieldName)){
                field.setAccessible(true);
                if(!isPrimitive(field)) {
                    if(isMap(field)){
                        field.set(obj, getMap(field, field.getType(), jsonObject.getJSONArray(fieldName)));
                    }else if(!isCollection(field)) {
                        field.set(obj, map(field.getType(), jsonObject.getJSONObject(fieldName)));
                    }else{
                        field.set(obj, getCollection(field, field.getType(), jsonObject.getJSONArray(fieldName)));
                    }
                }else {
                    setPrimitive(field, obj, jsonObject);
                }
            }
        }

        return obj;
    }


    @SneakyThrows
    private static Set<Row> map(Class<?>[] types, JSONArray jsonArray){

        Iterator<String> iterable;
        JSONObject jsonObject;
        Set<Row> rows = new HashSet<>();

        for (Object o : jsonArray) {
            jsonObject = (JSONObject) o;
            iterable = jsonObject.keys();
            String key;
            while(iterable.hasNext()){
                key = iterable.next();
                rows.add(Row.init(key, jsonObject.get(key), types));
            }
        }

        return rows;
    }


    @SneakyThrows
    private static <C> C getCollection(Field field, Class<C> collectionClazz, JSONArray jsonArray){

        C collectionInstance = getBaseInstanceCollection(collectionClazz).getDeclaredConstructor().newInstance();
        Method method = collectionClazz.getMethod("add", Object.class);
        method.setAccessible(true);
        Class<?> type = getGenericInstance(field);

        for (Object o : jsonArray) {
            if(type != null) {
                method.invoke(collectionInstance, map(type, (JSONObject) o));
            }else{
                method.invoke(collectionInstance, o);
            }
        }

        return collectionInstance;
    }


    @SneakyThrows
    private static <M> M getMap(Field field, Class<M> mapClazz, JSONArray jsonArray){

        M mapInstance = getBaseInstanceMap(mapClazz).getDeclaredConstructor().newInstance();
        Method method = mapInstance.getClass().getMethod("put", Object.class, Object.class);
        method.setAccessible(true);
        Class<?>[] types = getGenericInstances(field);
        Set<Row> rows = map(types, jsonArray);

        for (Row row : rows) {
            method.invoke(mapInstance, row.key, row.value);
        }

        return mapInstance;
    }




    private static boolean isPrimitive(Field field){
        return field.getType().isPrimitive() || field.getType() == String.class;
    }

    private static boolean isCollection(Field field){
        return Collection.class.isAssignableFrom(field.getType());
    }


    private static boolean isMap(Field field){
        return Map.class.isAssignableFrom(field.getType());
    }


    private static <C> Class<? extends C> getBaseInstanceCollection(Class<C> collectionClazz){
        if(List.class.isAssignableFrom(collectionClazz)){
            return (Class<? extends C>) ArrayList.class;
        }

        if(Set.class.isAssignableFrom(collectionClazz)){
            return (Class<? extends C>) HashSet.class;
        }

        throw new  IllegalArgumentException();
    }


    private static <C> Class<? extends C> getBaseInstanceMap(Class<C> mapClazz){
        if(Map.class.isAssignableFrom(mapClazz)){
            return (Class<? extends C>) HashMap.class;
        }

        throw new  IllegalArgumentException();
    }



    private static Class<?> getGenericInstance(Field field){
        Type type = field.getGenericType();
        if(type instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }
        return null;
    }

    private static Class<?>[] getGenericInstances(Field field){
        Type type = field.getGenericType();
        if(type instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return Stream.of(parameterizedType.getActualTypeArguments())
                    .map(t -> (Class<?>) t)
                    .toArray(Class[]::new);
        }
        return null;
    }


    private static <T> void setPrimitive(Field field, T instance, JSONObject jsonObject) throws IllegalAccessException {
        String fieldName = field.getName();
        if(field.getType() == short.class){
            field.set(instance, ((Integer) jsonObject.getInt(fieldName)).shortValue());
        }else if(field.getType() == byte.class){
            field.set(instance, ((Integer) jsonObject.getInt(fieldName)).byteValue());
        }else if(field.getType() == double.class){
            field.set(instance, jsonObject.getDouble(fieldName));
        }else if(field.getType() == float.class){
            field.set(instance, jsonObject.getFloat(fieldName));
        }else{
            field.set(instance, jsonObject.get(fieldName));
        }
    }


    @AllArgsConstructor
    @NoArgsConstructor
    private static class Row<K, V>{
        private K key;
        private V value;

        public static Row init(String key, Object value, Class<?>[] types){
            return new Row(types[0].cast(key), types[1].cast(value));
        }

    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Setter @Getter
    private static class Group{

        private long id;
        private String cypher;
        private int academicYear;
        private double number;

        private Set<Member> members;
        private Map<String, String> map;

        @Override
        public String toString() {
            return "Group{" +
                    "id=" + id +
                    ", cypher='" + cypher + '\'' +
                    ", academicYear=" + academicYear +
                    ", number=" + number +
                    ", members=" + members +
                    ", maps=" + map +
                    '}';
        }
    }


    private static class Member{

        private String firstName;
        private String lastName;

        @Override
        public String toString() {
            return "Member{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    '}';
        }
    }

}
