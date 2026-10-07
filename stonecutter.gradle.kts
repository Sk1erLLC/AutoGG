plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3" /* DO NOT EDIT */

stonecutter parameters {
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String
}
