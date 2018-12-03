package a4.reflection

object Reflection {
  def fieldsOf(instance: Any): List[(String, AnyRef)] =
    instance.getClass.getDeclaredFields.map(field => {
      field setAccessible true
      field.getName -> field.get(instance)
    }).toList
}
