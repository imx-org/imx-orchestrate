# Orchestrate

![Build status](https://github.com/dotwebstack/orchestrate/actions/workflows/build.yml/badge.svg)

## Architecture goals

- **Model-driven**: the mapping of one or more datasets to a single orchestrated dataset is specified on the level of the information model, regardless of the way the data is published (e.g. APIs, serialization formats). 
- **Heterogeneous data sources**: the orchestration engine must be able to retrieve data from data sources providing completely different types of interfaces. For example: one data source can be a REST API, while another data source can be a GraphQL API or a CSV file.

## Comparison with existing concepts

### Apollo Federation

Specification: https://www.apollographql.com/docs/federation/

1. Apollo Federation is GraphQL-only, requiring every data source to comply with the Apollo Federation standard, while Orchestrate supports heterogeneous data sources, which may or may not implement GraphQL.
2. Apollo Federation requires every sub model to be aware of its relations to other models (as part of the GraphQL schema), whereas orchestrate fully separates the mapping between information models from the models itself. 
3. Apollo Federation is primarily used for wiring models, where each object type lives (and stays) within its own schema. With orchestrate, you can combine objects by flattening, expanding or zipping multiple objects together.
4. Apollo Federation does not perform data modifications, such as type conversions or aggregations.

## License

This library is published under the [MIT License](LICENSE.md).
