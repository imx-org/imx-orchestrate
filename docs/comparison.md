## Comparison

This page describes how this library compares to existing open-source concepts.

### Apollo Federation

Specification: https://www.apollographql.com/docs/federation/

1. Apollo Federation is GraphQL-only, requiring every data source to comply with the Apollo Federation standard, while Orchestrate supports heterogeneous data sources, which may or may not implement GraphQL.
2. Apollo Federation requires every sub model to be aware of its relations to other models (as part of the GraphQL schema), whereas Orchestrate fully separates the mapping between information models from the models itself.
3. Apollo Federation is primarily used for wiring models, where each object type lives (and stays) within its own schema. With orchestrate, you can combine objects by flattening, expanding or zipping multiple objects together.
4. Apollo Federation does not provide data lineage, whereas Orchestrate tracks the full data lineage for every object instance.
5. Apollo Federation does not perform data modifications, such as type conversions or aggregations.
