

require "selenium-webdriver"

# Selenium doc:
#   http://www.rubydoc.info/gems/selenium-webdriver/0.0.28/Selenium/WebDriver/Driver
# - Download geckodriver
# - export PATH=/path/to/geckodriver:$PATH
module TestDSL
  def test(name, &block)
    # Init the driver to control Firefox
    @driver = Selenium::WebDriver.for :firefox
    # Start the test
    block.call
  rescue Selenium::WebDriver::Error::NoSuchElementError => e
    puts "Could not find element"
    puts e.message
  ensure
    # End test
    puts "Test finished!"
    @driver.quit
  end
  
  def go_to(url)
    @driver.navigate.to(url)    
  end

  def download(url, output)
    # return @driver.navigate.to(url)
    @driver.get(url)
    file = File.new(output, "w")
    file.puts(@driver.page_source)
    file.close
  end

  
  def fill(id, value)
    element = @driver.find_element(:name, id)
    element.clear
    element.send_keys(value)
  end

  def press(id, &block)
    element = @driver.find_element(:name, id)
    element.submit
    block.call
  end

  def submit(id, &block)
    element = @driver.find_element(:id, id)
    element.submit
    block.call
  end

  # Assert methods
  def title_must_be(value)
    if @driver.title != value
      raise "Title must be '#{value}' but is '#{@driver.title}'"
    end
  end

  def page_must_contain(value)
    puts @driver.page_source
    if ! @driver.page_source.include?(value)
      raise "Page does not contain '#{value}'"
    end
  end
end
