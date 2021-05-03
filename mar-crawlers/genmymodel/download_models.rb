
require 'curl'
require_relative 'indexer'

class Model
  attr_reader :href
  
  def initialize(href, m, extension)
    @href = href
    @json_model = m
    @extension = extension
  end
  
  def download(driver, folder)
    file_name = to_filename(@href, folder)
    if File.exists?(file_name)
      puts "File #{file_name} already downloaded"
      return false
    else
      resp = Curl.get @href
      File.write(file_name, resp.body_str)
      return true
    end
  end

  def to_filename(url, folder)
    id = @json_model['project_id']
    if id.nil?
      parts = @href.split("/")
      id = parts[parts.size - 2]
    end
    folder + "/" + id + "." + @extension
  end
end

def download_all(models, folder)
  models.each { |m|
    puts "Downloading #{m.href}"
    begin
      if m.download(self, folder)
        sleep(1)
      end
    rescue Net::ReadTimeout => e
      puts "Time out. Can't download #{m.href}"
    end
  }
end

if __FILE__ == $0
  if ARGV.size < 3
    puts "downloader model-type extension output_folder [first:last]"
    exit
  end

  init = nil
  last = nil
  if ARGV.size == 4
    parts = ARGV[2].split(":")
    init = parts[0].to_i
    last = parts[1].to_i
  end

  model_type = ARGV[0]
  genmymodel_extension, target_extension = ARGV[1].split(":")
  folder = ARGV[2]
  index = make_index(init, last)

  if not ['xmi', 'bpmn'].include?(genmymodel_extension)
    puts "Select extension: xmi or bpmn"
    return
  end

  if target_extension.nil?
    target_extension = genmymodel_extension
  end
  
  
  models = index[model_type]

  # links
  # projectId
  # name
  # creationDate
  # lastModificationDate

  to_download = []
  models.each { |m|
    model_link = m['links'].find { |l| l['rel'] == genmymodel_extension }
    unless model_link.nil?
      to_download << Model.new(model_link['href'], m, target_extension)
    end
  }

  puts "Collected #{to_download.size} models"
  download_all(to_download, folder)  
end
  
