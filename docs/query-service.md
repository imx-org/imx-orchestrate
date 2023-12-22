# Query service

When using the default gateway, a GraphQL endpoint is provided out-of-the-box. For every object type, the following types of read operations can be performed.

## Object query

Objects can be queried by their identifier (identifiers can be composite). Related objects can be expanded by selecting
relation properties. This could be performed over multiple levels.

```graphql
query ObjectQueryExample {
  address(id: "123") {
    id
    postalCode
    houseNumber
    geolocation
    isPartOfBuilding {
      id
      yearOfConstruction
    }
  }
}
```

## Collection query

Collections can be queries by a special query field with the `Collection` suffix. Collections can be filtered, using
multiple comparison operators. The supported operators can be different, depending on the data type of the property:
- All data types support the `equals` operator.
- Geometry data types support additional spatial operators: `intersects`, `touches`, `contains`, `within`, `overlaps`, `crosses`, `disjoint`

For now, collections can only be queried by direct properties. Paging and sorting is not yet supported.

```graphql
query CollectionQueryExample($filter: AddressFilter) {
  addressCollection(filter: $filter) {
    id
    postalCode
    houseNumber
  }
}
```

With payload:

```json
{
  "filter": {
    "geolocation": {
      "intersects": "POINT(4.8909244 52.3735142)"
    }
  }
}
```

For readability, the filter value is passed as a GraphQL variable with the name `filter`.

## Batch query

Multiple objects can be queried by a single operation, by passing multiple identifiers.

```graphql
query BatchQueryExample($keys: [AddressKey!]!) {
  addressBatch(keys: $keys) {
    id
    postalCode
    houseNumber
  }
}
```

With payload:

```json
{
  "keys": [
    { "id": "123" },
    { "id": "234" },
    { "id": "456" }
  ]
}
```

For readability, the set of keys is passed as a GraphQL variable with the name `keys`.