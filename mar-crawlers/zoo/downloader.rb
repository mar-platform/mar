# coding: utf-8
require 'nokogiri'
require 'curl'
require 'fileutils'
require_relative '../common/ruby_common'

def insert_model(db, name, link_href, description, date, tags, authors)
  model_id = name
  
  repo_id = 'atlanmod'
  results = db.query('SELECT id FROM repo_info WHERE id = ?', repo_id)
  repo = results.next
  if not repo
    db.execute('INSERT INTO repo_info(id) VALUES(?)', repo_id)
  end

  insert = <<-SQL
         INSERT INTO data(model_id, filename, name, download_url, repo_id, creation_date, last_update, description, topics, authors) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) 
 SQL
  
  db.execute(insert, model_id, name, name, link_href, repo_id, date, date, description, tags, authors)
end

if __FILE__ == $0
  if ARGV.size < 1
    puts "downloader output_folder"
    exit
  end

  output_folder = ARGV[0]
  
  
  url = 'https://web.imt-atlantique.fr/x-info/atlanmod/index.php?title=Ecore'
  resp = Curl.get url
  web = resp.body_str

  doc = Nokogiri::HTML.parse(web)

  #puts doc
  models = doc.xpath("//h3")

  data_folder = File.join(output_folder, 'data')
  output_db = File.join(output_folder, 'crawler.db')
  FileUtils.mkdir_p data_folder

  open_database(output_db) do |db| 
    # The structure of a model is
    # <h3>NAME</h3>
    # <hr/>
    # <p>DATE</p>
    # <p>DOMAIN</p>
    models.each { |m|
      if m.attribute('id')
        next
      end
      
      name = m.text
      date   = m.next.next.next.next
      domain = date.next
      domain_str = domain.children[1]
      description = domain.next
      description_str = description.children[1]
      additional = description.next
      additional_str = additional.children[2]
      authors = additional.next
      authors_str = authors.children[1]
      link_set = authors.next.next
      link = link_set.children.xpath("a").map { |a| a.attribute('href') }[0].to_s

      description_txt = description_str.text.sub("&nbsp;:", "").sub("Description :", "").sub(" : ", "").strip
      authors_txt = authors_str.text.sub("&nbsp;:", "").sub("Authors :", "").sub(" : ", "").strip
      additional_txt = nil
      if not additional_str.nil?
        additional_txt = additional_str.text.sub("&nbsp;:", "").sub("See :", "").sub(" : ", "").strip
      end
        
      tags = domain_str.text.sub("&nbsp;:", "").sub("Domain :", "").sub(" : ", "").split(',').map { |s| s.strip }.join(',')

      if not (additional_txt.nil? || additional_txt.empty?)
        description_txt = description_txt + " " + additional_txt
      end
      
      #puts m
      #puts date
      #puts domain
      #puts description
      #puts additional
      #puts authors
      #puts link

      parts = link.split('/')
      fname = parts[parts.size - 1].split('?')[0]

      puts fname
      puts "  #{description_txt}"
      puts "  #{tags}"
      puts "  #{authors_txt}"
      
      begin
        easy = Curl::Easy.new
        easy.follow_location = true
        easy.max_redirects = 3 
        easy.url = link
        easy.perform
        
        f = File.join(data_folder, fname)
        puts "Writing #{f}"
        File.write(f, easy.body_str)

        # For the moment do not parse the date
        
        insert_model(db, fname, link, description_txt, nil, tags, authors_txt)
      rescue => e
        puts e
      end
    }
  end
  
end

