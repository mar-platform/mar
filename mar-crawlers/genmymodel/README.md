
# Requirements

```
sudo apt-get install libcurl4-openssl-dev
sudo gem install curb

sudo apt-get install libsqlite3-dev
sudo gem install sqlite3
```

## Usage

This crawler works in four steps.

 1. Use `download_metadata.rb` to obtain all metadata from 
    https://app.genmymodel.com/api/projects/public
    This is downloaded in the form of `page.0xxx` files where `xxx` is
    the pagination number. 

 2. Use `indexer.rb` to analyse the page files and show the number of
    model per type.
   
 3. Download the models per type:
    Example:
    ```
      ruby download_models.rb EMF xmi:ecore ecore
    ```
 4. Update the crawler database
    The input is folder with files downloaded from GenMyModel, where each
	file follows the convetion [id.extension] where `id` is the GenMyModel
	id and `extension` could be `xmi, ecore, etc.`.
	```
    ruby metadata_gen.rb EMF ecore pages/ ecore-models/ path/to/db/crawler.db
    ```
