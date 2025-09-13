package com.levo.schema.request;

import com.levo.schema.entity.Schema;

public class SchemaWithContentWrapper {
    private Schema schema;
    private String content;

    public SchemaWithContentWrapper(Schema schema, String content) {
        this.schema = schema;
        this.content = content;
    }

    public Schema getSchema() {
        return schema;
    }

    public String getContent() {
        return content;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public void setContent(String content) {
        this.content = content;
    }
}