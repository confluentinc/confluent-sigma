# Sigma Syntax

Official sigma yaml syntax can be found here: https://github.com/SigmaHQ/sigma/wiki/Specification

## Supported Syntax

Confluent Sigma will parser the entire sigma model but some fields have no impact on functionality (metadata fields) and
some syntax is not yet supported.

### Unused Metadata fields
* `id`
* `related`
* `status`
* `description`
* `author`
* `references`
* `logsource.category`
* `logsource.definition`
* `fields`
* `falsepositives`
* `level`
* `tags`

### Used Fields

* `title` - Currently this field is used for the record key when the sigma rules are loaded with the cli.  If a rule
  with the same title is published it will be considered to be a replacement of the prexisting one.
* `logsource`
  * `product` - Confluent Sigma allows you to configure a filter to only use sigma rules with a specific product tag
    `sigma.rule.filter.product`
  * `service` - Confluent Sigma allows you to configure a filter to only use sigma rules with a specific product tag
    `sigma.rule.filter.service`
* `detection` - Used to specify what matches this sigma rule.  More details on what is supported for the `detection` 
    field below.
  * `timeframe` - Specifies the window of time that the condition should be matches against.  If the `condition` does
  have a `count()` this will be ignore
  * `condition` - used to specify how to combine the detection identifiers 

### `detection` [syntax](https://github.com/SigmaHQ/sigma/wiki/Specification#detection)

Confluent Sigma supports both Lists and Maps. As per the specificaiton lists are logically ORed and Maps are logically 
ANDed.  More details can be found in the Sigma spec

#### Value Modifiers

Value modifiers are used to transform how a record value is interpreted in detection process.  Currently we do NOT 
support chaining the modifiers with multiple pipes.

Supported value modifiers

* `contains`
* `endswith`
* `startswith`
* `re` 

Unsupported value modifiers

* `all`
* `base64`
* `base64offset`
* `utf16le`
* `utf16be`
* `utf16`
